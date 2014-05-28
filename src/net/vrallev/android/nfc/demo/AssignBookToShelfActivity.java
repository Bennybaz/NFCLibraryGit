package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.*;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.*;
import libalg.BranchAndBound;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Benny on 06/04/2014.
 */
public class AssignBookToShelfActivity extends Activity {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private NfcAdapter mNfcAdapter;
    Context context;
    ListView lv;
    public MySimpleArrayAdapter adapter;
    private Button writeToShelfBtn;
    ArrayList<Book> b = new ArrayList<Book>();
    StringBuilder allIDs=new StringBuilder();
    Dialog dialog;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag mytag;
    TextView shelf;
    String barcode;
    Dialog directDialog;
    int successFlag=0;
    int shelfFlag=0;
    int duplicateFlag=0;

    // JSON parser class
    private JSONParser jsonParser = new JSONParser();
    private JSONParser jsonParser2 = new JSONParser();

    // username in db url
    private static final String url_book_barcode_for_details = "http://nfclibrary.site40.net/barcode_for_title_and_author.php";
    private static final String url_book_to_shelf = "http://nfclibrary.site40.net/attach_book_to_shelf.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCT = "book";
    //private static final String TAG_PID = "sid";
    private static final String TAG_NAME = "name";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assign_shelf);
        setTitle("Assign Books To Shelf");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        context = this;

        writeToShelfBtn = (Button) findViewById(R.id.writeShelfBtn);
        shelf = (TextView) findViewById(R.id.shelfTextView);
        lv = (ListView) findViewById(R.id.listViewShelf);
        adapter = new MySimpleArrayAdapter(this, b);
        lv.setAdapter(adapter);


        writeToShelfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(b.isEmpty()) Toast.makeText(context,"You need to scan some books first", Toast.LENGTH_SHORT).show();
                else new UpdateBookLocation().execute();

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
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
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
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
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

            if (result != null) {
                String type = result.substring(0,2);
                //int row = Integer.parseInt(result.substring(2,4));

                if(type.equals("BK")){
                    if(shelfFlag==0) Toast.makeText(context,"Please Scan a Shelf First", Toast.LENGTH_SHORT).show();
                    else {
                        super.onPostExecute(result);

                        barcode = result.substring(2);


                        // Getting complete user details in background thread
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            new GetBookBarcode().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        else
                            new GetBookBarcode().execute();
                    }
                }
                else if(type.equals("SH")){
                    super.onPostExecute(result);
                    shelfFlag=1;
                    shelf.setText(result.substring(2,4));
                    lv.setVisibility(1);
                    writeToShelfBtn.setVisibility(1);
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

                        JSONObject json2 = jsonParser.makeHttpRequest(
                                url_book_barcode_for_details, "GET", params);

                        // json success tag
                        if(json2!=null) {
                            success = json2.getInt(TAG_SUCCESS);
                            if (success == 1) {
                                // successfully received product details
                                JSONArray productObj = json2.getJSONArray(TAG_PRODUCT); // JSON Array

                                // get first user object from JSON Array
                                JSONObject product = productObj.getJSONObject(0);

                                for(int i=0;i<b.size();i++){
                                    if(b.get(i).getBarcode().equals(barcode)) duplicateFlag=1;
                                }

                                if(duplicateFlag==0) {

                                    Book bk = new Book();
                                    bk.setBarcode(barcode);
                                    bk.setName(product.getString("title"));
                                    bk.setAuthor(product.getString("author"));
                                    b.add(bk);
                                    adapter.notifyDataSetChanged();
                                }
                                else Toast.makeText(context,"Book Already Exists", Toast.LENGTH_SHORT).show();
                            } else {
                                // product with pid not found
                                Toast.makeText(context,"Please Try Again", Toast.LENGTH_SHORT).show();
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


    class UpdateBookLocation extends AsyncTask<String, String, String> {

       /* *
         * Getting product details in background thread
         **/
        protected String doInBackground(String... params) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;
                    for(int i=0; i< b.size(); i++)
                    {
                        try{
                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                            Integer sh = Integer.parseInt(shelf.getText().toString());

                            params.add(new BasicNameValuePair("shelf", sh.toString()));
                            params.add(new BasicNameValuePair("barcode", b.get(i).getBarcode().toString()));

                            // getting student details by making HTTP request
                            // Note that product details url will use GET request
                            JSONObject json = jsonParser2.makeHttpRequest(
                                    url_book_to_shelf, "GET", params);

                            // json success tag
                            if(json!=null) {
                                success = json.getInt(TAG_SUCCESS);
                                if (success == 1) {

                                    successFlag=1;

                                } else {
                                    // product with pid not found
                                    Toast.makeText(context,"Please Try Again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    if(successFlag==1) {
                        directDialog = new Dialog(context);
                        directDialog.setContentView(R.layout.direction_dialog);
                        directDialog.setTitle("Success");
                        TextView bookCase = (TextView) directDialog.findViewById(R.id.textBC);
                        TextView shelff = (TextView) directDialog.findViewById(R.id.textShelf);
                        bookCase.setText("");
                        shelff.setText("Books were assigned");
                        ImageView image = (ImageView) directDialog.findViewById(R.id.directImage);
                        image.setImageResource(R.drawable.success);
                        Button dialogButtonCancel = (Button) directDialog.findViewById(R.id.directionButtonCancel);
                        Button dialogButtonScan = (Button) directDialog.findViewById(R.id.directionButtonScan);

                        // if button is clicked, close the custom dialog
                        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                directDialog.dismiss();
                            }
                        });
                        directDialog.show();
                        b.clear();
                        adapter.notifyDataSetChanged();
                        shelf.setText("");
                    }
                }
            });

            return null;
        }
    }
}
