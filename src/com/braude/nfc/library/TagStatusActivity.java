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
import android.widget.LinearLayout;
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
 * Created by Lidor on 28/05/14.
 */
public class TagStatusActivity extends Activity {

    Context context;

    private Button checkStatusBtn;

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private NfcAdapter mNfcAdapter;

    //for book layout
    TextView title;
    TextView author;
    TextView publisher;
    TextView year;
    TextView shelf;
    TextView barcode;
    TextView status;
    TextView location;

    //for shelf layout
    TextView tagCode;
    TextView shelfT;
    TextView sectorT;
    TextView caseT;

    //for user layout
    TextView id;
    TextView username;
    TextView phone;
    TextView email;
    TextView rank;


    LinearLayout bookLayout;
    LinearLayout shelfLayout;
    LinearLayout userLayout;

    int flag=0; // type of scanned tag flag
    int rankFlag;
    String bar;

    private JSONParser jsonParser = new JSONParser();

    // book in db url
    private static final String url_book_tag_details = "http://nfclibrary.site40.net/barcode_for_book_details.php";
    private static final String url_shelf_tag_details = "http://nfclibrary.site40.net/code_for_shelf_details.php";
    private static final String url_user_tag_details = "http://nfclibrary.site40.net/code_for_user_details.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tag_status);
        setTitle("Tag Status");

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        context = this;

        rankFlag = getIntent().getIntExtra("rankFlag",0);

        title = (TextView) findViewById(R.id.status_titleTextView);
        author = (TextView) findViewById(R.id.status_authorTextView);
        publisher = (TextView) findViewById(R.id.status_publisherTextView);
        year = (TextView) findViewById(R.id.status_yearTextView);
        shelf = (TextView) findViewById(R.id.status_shelfTextView);
        barcode = (TextView) findViewById(R.id.status_deweyTextView);
        status = (TextView) findViewById(R.id.status_statusTextView);
        location = (TextView) findViewById(R.id.location_of_bookTextView);

        tagCode = (TextView) findViewById(R.id.status_tagCodeTextView);
        shelfT = (TextView) findViewById(R.id.status_bookShelfTextView);
        sectorT = (TextView) findViewById(R.id.status_bookSectorTextView);
        caseT = (TextView) findViewById(R.id.status_bookCaseTextView);

        id = (TextView) findViewById(R.id.status_idTextView);
        username = (TextView) findViewById(R.id.status_userNameTextView);
        phone = (TextView) findViewById(R.id.status_phoneTextView);
        email = (TextView) findViewById(R.id.status_emailTextView);
        rank = (TextView) findViewById(R.id.status_userRankTextView);



        bookLayout = (LinearLayout) findViewById(R.id.bookStatusLayout);
        shelfLayout = (LinearLayout) findViewById(R.id.ShelfStatusLayout);
        userLayout = (LinearLayout) findViewById(R.id.UserStatusLayout);

        bookLayout.setVisibility(View.INVISIBLE);
        shelfLayout.setVisibility(View.INVISIBLE);
        userLayout.setVisibility(View.INVISIBLE);


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
                String type = result.substring(0, 2);
                //int row = Integer.parseInt(result.substring(2,4));

                if (rankFlag == 0) {

                    if (type.equals("BK")) {
                        super.onPostExecute(result);
                        bookLayout.setVisibility(View.VISIBLE);
                        shelfLayout.setVisibility(View.INVISIBLE);
                        userLayout.setVisibility(View.INVISIBLE);
                        new GetBookDetails().execute();
                        bar = result.substring(2);
                    } else if (type.equals("SH")) {
                        super.onPostExecute(result);
                        shelfLayout.setVisibility(View.VISIBLE);
                        bookLayout.setVisibility(View.INVISIBLE);
                        userLayout.setVisibility(View.INVISIBLE);
                        new GetShelfDetails().execute();
                        bar = result.substring(2);
                    } else if (type.equals("LB") || type.equals("ST")) {
                        super.onPostExecute(result);
                        userLayout.setVisibility(View.VISIBLE);
                        bookLayout.setVisibility(View.INVISIBLE);
                        shelfLayout.setVisibility(View.INVISIBLE);
                        new GetUserDetails().execute();
                        bar = result.substring(2);
                    } else Toast.makeText(context, "Please Scan a Book/Shelf/User Tag Only", Toast.LENGTH_SHORT).show();
                }
                else if (rankFlag==1){
                    if (type.equals("BK")) {
                        super.onPostExecute(result);
                        bookLayout.setVisibility(View.VISIBLE);
                        shelfLayout.setVisibility(View.INVISIBLE);
                        userLayout.setVisibility(View.INVISIBLE);
                        new GetBookDetails().execute();
                        bar = result.substring(2);
                    }
                    else Toast.makeText(context, "Please Scan a Book Tag Only", Toast.LENGTH_SHORT).show();

                }

            }
        }
    }

    /**
     * Background Async Task to Get complete search details
     * */
    class GetBookDetails extends AsyncTask<String, String, String> {

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
                        params.add(new BasicNameValuePair("barcode", bar));

                        // getting student details by making HTTP request
                        // Note that product details url will use GET request

                        JSONObject json = jsonParser.makeHttpRequest(
                                url_book_tag_details, "GET", params);

                        // json success tag
                        if(json!=null) {
                            success = json.getInt(TAG_SUCCESS);
                            if (success == 1) {
                                // successfully received product details
                                JSONArray productObj = json.getJSONArray("book"); // JSON Array

                                // get first user object from JSON Array
                                JSONObject product = productObj.getJSONObject(0);

                                //set the book details
                                title.setText(product.getString("title"));
                                author.setText(product.getString("author"));
                                publisher.setText(product.getString("publisher"));
                                year.setText(product.getString("year"));
                                shelf.setText(product.getString("shelfD"));
                                barcode.setText(product.getString("barcode"));

                                int standd = product.getInt("standT");
                                String bc=new String();

                                if(standd==1) bc="2";
                                if(standd==2) bc="3";
                                if(standd==3) bc="4";
                                if(standd==4) bc="5";
                                if(standd==5) bc="6";
                                if(standd==6) bc="7";
                                if(standd==7) bc="8";
                                if(standd==8) bc="9";
                                if(standd==9) bc="10";
                                if(standd==10) bc="11";
                                if(standd==11) bc="13";
                                if(standd==12) bc="14";
                                if(standd==13) bc="33";
                                if(standd==14) bc="34";
                                if(standd==15) bc="35";
                                if(standd==16) bc="36";
                                if(standd==17) bc="37";
                                if(standd==18) bc="38";
                                if(standd==19) bc="39";
                                if(standd==20) bc="40";
                                if(standd==21) bc="41";
                                if(standd==22) bc="42";
                                if(standd==23) bc="43";
                                if(standd==24) bc="44";

                                location.setText("Shelf: "+product.getString("shelfT")+", Sector: "+product.getString("sectorT")+", Stand: "+bc);
                                //status.setText(product.getString("status"));
                                if(product.getString("status").equals("ok")) {
                                    status.setText("Book exists on shelf");
                                    status.setTextColor(getResources().getColor(R.color.emerald));
                                }
                                else {
                                    status.setText("Book is already borrowed");
                                    status.setTextColor(getResources().getColor(R.color.reddd));
                                }

                            } else {
                                Toast.makeText(context, "Error: Missing Data About The Book", Toast.LENGTH_SHORT).show();
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

    /**
     * Background Async Task to Get complete search details
     * */
    class GetShelfDetails extends AsyncTask<String, String, String> {

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
                        params.add(new BasicNameValuePair("barcode", bar));

                        // getting student details by making HTTP request
                        // Note that product details url will use GET request

                        JSONObject json = jsonParser.makeHttpRequest(
                                url_shelf_tag_details, "GET", params);

                        // json success tag
                        if(json!=null) {
                            success = json.getInt(TAG_SUCCESS);
                            if (success == 1) {
                                // successfully received product details
                                JSONArray productObj = json.getJSONArray("book"); // JSON Array

                                // get first user object from JSON Array
                                JSONObject product = productObj.getJSONObject(0);

                                //set the shelf details

                                int standd = product.getInt("stand");
                                String bc=new String();

                                if(standd==1) bc="2";
                                if(standd==2) bc="3";
                                if(standd==3) bc="4";
                                if(standd==4) bc="5";
                                if(standd==5) bc="6";
                                if(standd==6) bc="7";
                                if(standd==7) bc="8";
                                if(standd==8) bc="9";
                                if(standd==9) bc="10";
                                if(standd==10) bc="11";
                                if(standd==11) bc="13";
                                if(standd==12) bc="14";
                                if(standd==13) bc="33";
                                if(standd==14) bc="34";
                                if(standd==15) bc="35";
                                if(standd==16) bc="36";
                                if(standd==17) bc="37";
                                if(standd==18) bc="38";
                                if(standd==19) bc="39";
                                if(standd==20) bc="40";
                                if(standd==21) bc="41";
                                if(standd==22) bc="42";
                                if(standd==23) bc="43";
                                if(standd==24) bc="44";

                                tagCode.setText(bar);
                                shelfT.setText(product.getString("shelf"));
                                sectorT.setText(product.getString("sector"));
                                caseT.setText(bc.toString());

                            } else {
                                Toast.makeText(context, "Error: No Shelf Details", Toast.LENGTH_SHORT).show();
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


    /**
     * Background Async Task to Get complete search details
     * */
    class GetUserDetails extends AsyncTask<String, String, String> {

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
                        params.add(new BasicNameValuePair("barcode", bar));

                        // getting student details by making HTTP request
                        // Note that product details url will use GET request

                        JSONObject json = jsonParser.makeHttpRequest(
                                url_user_tag_details, "GET", params);

                        // json success tag
                        if(json!=null) {
                            success = json.getInt(TAG_SUCCESS);
                            if (success == 1) {
                                // successfully received product details
                                JSONArray productObj = json.getJSONArray("book"); // JSON Array

                                // get first user object from JSON Array
                                JSONObject product = productObj.getJSONObject(0);

                                //set the shelf details

                                id.setText(bar);
                                username.setText(product.getString("username"));
                                phone.setText(product.getString("phone"));
                                email.setText(product.getString("email"));
                                rank.setText(product.getString("type"));

                            } else {
                                Toast.makeText(context, "Error: No User Details", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            title.setText("");
            author.setText("");
            publisher.setText("");
            year.setText("");
            shelf.setText("");
            barcode.setText("");
            status.setText("");

            tagCode.setText("");
            shelfT.setText("");
            sectorT.setText("");
            caseT.setText("");

            id.setText(bar);
            username.setText("");
            phone.setText("");
            email.setText("");
            rank.setText("");

            flag=0;
        }
        return super.onKeyDown(keyCode, event);
    }
}