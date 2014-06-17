package com.braude.nfc.library;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.*;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Lidor on 28/04/14.
 */
public class ShelfManagementActivity extends Activity {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private NfcAdapter mNfcAdapter;
    Context context;
    ListView lv;
    public MySimpleArrayAdapter adapter;
    ArrayList<Book> scannedBooks = new ArrayList<Book>();
    ArrayList<Book> shelfBooks = new ArrayList<Book>();

    Button shelfManage;
    Button simulationBtn;
    TextView shelfText;

    private int shelfFlag=0;
    String barcode;
    int flag=0;
    int simulationFlag=0;

    // JSON parser class
    private JSONParser jsonParser = new JSONParser();

    // username in db url
    private static final String url_book_barcode_for_details = "http://nfclibrary.site40.net/barcode_for_title_and_author.php";
    private static final String url_shelf_code_for_books = "http://nfclibrary.site40.net/shelf_code_for_its_book_barcodes.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCT = "book";
    private static final String TAG_NAME = "name";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shelf_management);
        setTitle("Shelf Management");

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        context = this;

        shelfText = (TextView) findViewById(R.id.shelfScannedText);
        shelfManage = (Button) findViewById(R.id.shelfManageBtn);
        simulationBtn = (Button) findViewById(R.id.simulationForShelfManagementBtn);

        simulationFlag=0;
        lv = (ListView) findViewById(R.id.managementList);
        adapter = new MySimpleArrayAdapter(this, scannedBooks);
        lv.setAdapter(adapter);


        shelfManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(scannedBooks.size()==0 && shelfText.getText().equals(""))
                    Toast.makeText(context,"Please Scan Books and Shelf Tag",Toast.LENGTH_LONG).show();
                else if(scannedBooks.size()==0)
                    Toast.makeText(context,"Please Scan Books",Toast.LENGTH_LONG).show();
                else if(shelfText.getText().equals(""))
                    Toast.makeText(context,"Please Scan Shelf Tag",Toast.LENGTH_LONG).show();

                if(scannedBooks.size()>0 && !shelfText.getText().equals(""))
                    new GetBookOfShelf().execute();

            }
        });

        simulationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(simulationFlag==0)
                {
                    new GetBookBarcodeForSimulation().execute();
                    simulationFlag=1;
                }
                else Toast.makeText(context,"Simulation is already running",Toast.LENGTH_SHORT).show();

            }
        });
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();

		/*
		 * It's important, that the activity is in the foreground (resumed). Otherwise
		 * an IllegalStateException is thrown.
		 */
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
		/*
		 * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
		 */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

   
    @Override
    protected void onNewIntent(Intent intent) {
		/*
		 * This method gets called, when a new Intent gets associated with the current activity instance.
		 * Instead of creating a new activity, onNewIntent will be called. For more information have a look
		 * at the documentation.
		 *
		 * In our case this method gets called, when the user attaches a Tag to the device.
		 */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    new NdefReaderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tag);
                else
                    new NdefReaderTask().execute(tag);

                // new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        new NdefReaderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tag);
                    else
                        new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    /**
     * @param activity The corresponding {@link android.app.Activity} requesting the foreground dispatch.
     * @param adapter The {@link android.nfc.NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding {@link BaseActivity} requesting to stop the foreground dispatch.
     * @param adapter The {@link android.nfc.NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    public void nfcStatusChanged(View view) {
        startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 2);
    }

    // create a distance matrix according to the input


    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     *
     * @author Ralf Wondratschek
     *
     */

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
			/*
			 * See NFC forum specification for "Text Record Type Definition" at 3.2.1
			 *
			 * http://www.nfc-forum.org/specs/
			 *
			 * bit_7 defines encoding
			 * bit_6 reserved for future use, must be 0
			 * bit_5..0 length of IANA language code
			 */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {

            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(300);

            if (result != null) {
                String type = result.substring(0,2);
                //int row = Integer.parseInt(result.substring(2,4));


                if(type.equals("BK") ){
                    if(shelfFlag==1) {
                        super.onPostExecute(result);

                        barcode = result.substring(2);

                        int flagg=0;
                        for(int i=0; i<scannedBooks.size(); i++) {
                            if (scannedBooks.get(i).getBarcode().equals(barcode)) flagg = 1;
                        }

                        if(flagg==1) Toast.makeText(context, "Book Already Exists", Toast.LENGTH_SHORT).show();
                        else new GetBookBarcode().execute();
                    }
                    else Toast.makeText(context,"Please Scan a Shelf First", Toast.LENGTH_SHORT).show();
                }
                else if(type.equals("SH")){
                    shelfFlag=1;
                    super.onPostExecute(result);
                    shelfText.setText(result.substring(2));
                    lv.setVisibility(View.VISIBLE);
                    shelfManage.setVisibility(View.VISIBLE);
                }
                else Toast.makeText(context,"Please Scan a Book/Shelf Tag Only", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class GetBookBarcode extends AsyncTask<String, String, String> {

        /**
         * Getting product details in background thread
         * */
        protected String doInBackground(String... params) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;

                    try {
                        // Building Parameters
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("barcode", barcode));

                        // getting student details by making HTTP request
                        // Note that product details url will use GET request

                        JSONObject json = jsonParser.makeHttpRequest(
                                url_book_barcode_for_details, "GET", params);

                        // json success tag
                        if(json!=null) {
                            success = json.getInt(TAG_SUCCESS);
                            if (success == 1) {
                                // successfully received product details
                                JSONArray productObj = json.getJSONArray(TAG_PRODUCT); // JSON Array

                                // get first user object from JSON Array
                                JSONObject product = productObj.getJSONObject(0);

                                Book bk = new Book();
                                bk.setBarcode(barcode);
                                bk.setName(product.getString("title"));
                                bk.setAuthor(product.getString("author"));
                                bk.setYear(product.getString("year"));

                                scannedBooks.add(bk);
                                adapter.notifyDataSetChanged();

                                lv.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Select the last row so it will scroll into view...
                                        lv.setSelection(lv.getCount() - 1);
                                    }
                                });

                            } else {
                                // product with pid not found
                                Toast.makeText(context, "Please Try Again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return null;
        }
    }


    class GetBookOfShelf extends AsyncTask<String, String, String> {

        /**
         * Getting product details in background thread
         * */
        protected String doInBackground(String... params) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;

                    try {
                        // Building Parameters
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("shelf", shelfText.getText().toString()));

                        // getting student details by making HTTP request
                        // Note that product details url will use GET request

                        JSONObject json = jsonParser.makeHttpRequest(
                                url_shelf_code_for_books, "GET", params);

                        // json success tag
                        if(json!=null) {
                            success = json.getInt(TAG_SUCCESS);
                            if (success == 1) {
                                // successfully received product details
                                JSONArray productObj = json
                                        .getJSONArray("books"); // JSON Array

                                for(int i=0; i<productObj.length(); i++)
                                {
                                    // get first user object from JSON Array
                                    JSONObject product = productObj.getJSONObject(i);
                                    Book b = new Book();
                                    b.setBarcode(product.getString("barcode"));
                                    shelfBooks.add(b);
                                }

                                for(int i=0; i<scannedBooks.size(); i++)
                                {
                                    int index=containsBook(shelfBooks, scannedBooks.get(i).getBarcode());
                                    if(index != -1)
                                    {
                                        shelfBooks.remove(index);
                                        scannedBooks.remove(i);
                                        i--;
                                    }
                                }
                                //after this "for", books that don't belong to the shelf will be in the array list of scannedBooks

                                Intent intent = new Intent(ShelfManagementActivity.this, ShelfManagementResult.class);
                                intent.putParcelableArrayListExtra("bookList",scannedBooks);
                                startActivity(intent);

                            } else {
                                // product with pid not found
                                Toast.makeText(context, "Please Try Again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return null;
        }
    }

    // function gets array list and barcode and checks if the list contains the barcode
    //function returns position of the barcode if it exists, or -1 if it does not exists
    public int containsBook(ArrayList<Book> b, String bar)
    {
        for(int i=0; i<b.size(); i++)
        {
            String currentBar=b.get(i).getBarcode();
            if(currentBar.equals(bar))
                return i;
        }
        return -1;
    }


    class GetBookBarcodeForSimulation extends AsyncTask<String, String, String> {

        /**
         * Getting product details in background thread
         * */
        protected String doInBackground(String... params) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;

                    ArrayList<String> simBarcode = new ArrayList<String>();

                    shelfText.setText("4");
                    simBarcode.add("624-10");
                    simBarcode.add("1298-10");
                    simBarcode.add("602-10");
                    simBarcode.add("1568-20");

                    for(int i=0; i<simBarcode.size(); i++)
                    {
                        barcode=simBarcode.get(i);

                        try {
                            // Building Parameters
                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                            params.add(new BasicNameValuePair("barcode", barcode));

                            // getting student details by making HTTP request
                            // Note that product details url will use GET request

                            JSONObject json = jsonParser.makeHttpRequest(
                                    url_book_barcode_for_details, "GET", params);

                            // json success tag
                            if(json!=null) {
                                success = json.getInt(TAG_SUCCESS);
                                if (success == 1) {
                                    // successfully received product details
                                    JSONArray productObj = json.getJSONArray(TAG_PRODUCT); // JSON Array

                                    // get first user object from JSON Array
                                    JSONObject product = productObj.getJSONObject(0);

                                    Book bk = new Book();
                                    bk.setBarcode(barcode);
                                    bk.setName(product.getString("title"));
                                    bk.setAuthor(product.getString("author"));
                                    bk.setYear(product.getString("year"));


                                    for(int j=0; j<scannedBooks.size(); j++) {
                                        if(scannedBooks.get(j).getBarcode().equals(barcode))
                                            flag=1;
                                        break;
                                    }

                                    if(flag==0) {
                                        scannedBooks.add(bk);
                                        adapter.notifyDataSetChanged();
                                    }

                                } else {
                                    // product with pid not found
                                    Toast.makeText(context, "Please Try Again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            return null;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            scannedBooks.clear();
            shelfBooks.clear();
            adapter.notifyDataSetChanged();
        }
        return super.onKeyDown(keyCode, event);
    }

}