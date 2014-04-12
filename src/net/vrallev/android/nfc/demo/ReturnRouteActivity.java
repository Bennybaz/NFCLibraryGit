package net.vrallev.android.nfc.demo;

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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.*;
import libalg.BranchAndBound;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

/**
 * Created by Benny on 24/03/2014.
 */
public class ReturnRouteActivity extends Activity {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private int numberOfNodes;
    private Stack<Integer> stack;
    public double[][] adjacency_matrix;
    Context context;
    public ArrayList<Integer> sectors  = new ArrayList<Integer>();
    ListView lv;
    public MySimpleArrayAdapter adapter;
    ArrayList<Book> b = new ArrayList<Book>();

    Button calcBtn;
    private NfcAdapter mNfcAdapter;
    String barcode;
    int flag=0;

    // JSON parser class
    private JSONParser jsonParser = new JSONParser();
    private JSONParser jsonParser2 = new JSONParser();
    private JSONParser jsonParser3 = new JSONParser();

    // username in db url
    private static final String url_book_barcode_for_details = "http://nfclibrary.site40.net/barcode_for_title_and_author.php";
    private static final String url_book_barcode_for_sector = "http://nfclibrary.site40.net/barcode_to_sector.php";
    private static final String url_return_book_by_barcode = "http://nfclibrary.site40.net/return_book_by_barcode.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCT = "book";
    //private static final String TAG_PID = "sid";
    private static final String TAG_NAME = "name";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.return_route);
        setTitle("Return Books");
        context = this;

        calcBtn = (Button) findViewById(R.id.calcRouteBtn);
        lv = (ListView) findViewById(R.id.listView);
        adapter = new MySimpleArrayAdapter(this, b);
        lv.setAdapter(adapter);


        calcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new UpdateBookStatus().execute();
                try {
                    double[][] data2 = getDoubleTwoDimArray("dump.txt");
                    if(sectors.get(0)!=1) sectors.add(1);

                    Collections.sort(sectors);
                    adjacency_matrix = new double[sectors.size() + 1][sectors.size() + 1];

                    createAdj(data2, sectors);

                    BranchAndBound bnb = new BranchAndBound(adjacency_matrix,0, sectors);
                    String result = bnb.execute();
                    Toast.makeText(context, result,Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        handleIntent(getIntent());
    }

    public void createAdj(double[][] input, ArrayList<Integer> sectors){

        double[] row = new double[input.length];
        int i=0;

        while(i<sectors.size()){
            row = input[sectors.get(i)-1];
            int j=0;
            while(j<sectors.size()){
                adjacency_matrix[i][j]=row[sectors.get(j)-1];
                j++;
            }
            i++;
        }
    }

    public double[][] getDoubleTwoDimArray(String fileName) throws IOException {

        double[][] data = new double[0][0];

        InputStream databaseInputStream = getResources().openRawResource(R.raw.dump);
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(databaseInputStream));
        int rows = Integer.parseInt(reader.readLine().toString());
        int cols = Integer.parseInt(reader.readLine().toString());

        data = new double[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                data[row][col] = Double.parseDouble(reader.readLine().toString());
            }
        }
        databaseInputStream.close();
        String result = sb.toString();
        return data;
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
                new NdefReaderTask().execute(tag);

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
        startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS),2 );
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
                barcode=result.substring(2);

                if(type.equals("BK")){
                    super.onPostExecute(result);


                    // Getting complete user details in background thread
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        new GetBookSector().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        new GetBookSector().execute();
                }
                else Toast.makeText(context,"This is not a Book Tag",Toast.LENGTH_SHORT).show();

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

                                Book bk = new Book();
                                bk.setBarcode(barcode);
                                bk.setName(product.getString("title"));
                                bk.setAuthor(product.getString("author"));
                                for(int i=0; i<b.size(); i++) {
                                    if(b.get(i).getBarcode().equals(barcode))
                                        flag=1;
                                    break;
                                }

                                if(flag==0) {
                                    b.add(bk);
                                    adapter.notifyDataSetChanged();
                                }

                            } else {
                                // product with pid not found
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

    class GetBookSector extends AsyncTask<String, String, String> {

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

                        JSONObject json2 = jsonParser2.makeHttpRequest(
                                url_book_barcode_for_sector, "GET", params);

                        // json success tag
                        if(json2!=null) {
                            success = json2.getInt(TAG_SUCCESS);
                            if (success == 1) {

                                // successfully received product details
                                JSONArray productObj = json2.getJSONArray("reader"); // JSON Array

                                // get first user object from JSON Array
                                JSONObject product = productObj.getJSONObject(0);


                                int sector = product.getInt("sector");

                                if(!sectors.contains(sector)) {
                                    sectors.add(sector);
                                }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                        new GetBookBarcode().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    else
                                        new GetBookBarcode().execute();




                            } else {
                                // product with pid not found
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

    class UpdateBookStatus extends AsyncTask<String, String, String> {

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
                            params.add(new BasicNameValuePair("barcode", b.get(i).getBarcode().toString()));

                            // getting student details by making HTTP request
                            // Note that product details url will use GET request
                            JSONObject json = jsonParser3.makeHttpRequest(
                                    url_return_book_by_barcode, "GET", params);

                            Toast.makeText(context, json.toString(), Toast.LENGTH_SHORT).show();

                            // json success tag
                            if(json!=null) {
                                success = json.getInt(TAG_SUCCESS);
                                if (success == 1) {
                                    // successfully received product details
                                    //JSONArray productObj = json
                                    //       .getJSONArray(TAG_PRODUCT); // JSON Array

                                    // get first user object from JSON Array
                                    //JSONObject product = productObj.getJSONObject(0);
                                    Toast.makeText(context,"CHANGED",Toast.LENGTH_LONG).show();

                                } else {
                                    // product with pid not found
                                }
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    b.clear();
                    adapter.notifyDataSetChanged();
                }
            });

            return null;
        }
    }
}
