package com.braude.nfc.library;

import android.app.Activity;
import android.app.Dialog;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Benny on 22/03/2014.
 */
public class SearchResultActivity extends Activity{

    Context context = this;
    private Button getDirectionsBtn;
    private Button borrowBookBtn;
    private Dialog directDialog;
    private Button addToCartBtn;
    private Dialog borrowDialog;

    TextView title;
    TextView author;
    TextView publisher;
    TextView year;
    TextView shelf;
    TextView barcode;
    TextView status;
    TextView isTagged;

    private int borrowBtnClicked=0;
    private int successFlag=0;

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private NfcAdapter mNfcAdapter;


    ArrayList<Book> books; //contains the books from the search query
    private int pos; //position of specific book
    private Book bk; //the chosen book

    private JSONParser jsonParser = new JSONParser();
    private JSONParser jsonParser2 = new JSONParser();

    // book details in db url
    private static final String url_book_barcode_for_sector = "http://nfclibrary.site40.net/barcode_to_sector.php";
    private static final String url_book_tag_details = "http://nfclibrary.site40.net/barcode_for_book_details.php";
    private static final String url_book_borrow = "http://nfclibrary.site40.net/borrow_book_by_barcode.php";

    private String bar;
    private int alreadyAddedFlag=0;


    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    String FILENAME="cart";

    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result);
        setTitle("Book Info");

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        context = this;

        getDirectionsBtn = (Button) findViewById(R.id.getDirectionsBtn);
        borrowBookBtn = (Button) findViewById(R.id.borrowBookBtn);
        addToCartBtn = (Button) findViewById(R.id.addToCartBtn);

        books =  getIntent().getParcelableArrayListExtra("bookList");
        pos =  getIntent().getIntExtra("position",0);
        bk = books.get(pos);

        title = (TextView) findViewById(R.id.titleTextView);
        author = (TextView) findViewById(R.id.authorTextView);
        publisher = (TextView) findViewById(R.id.publisherTextView);
        year = (TextView) findViewById(R.id.yearTextView);
        shelf = (TextView) findViewById(R.id.shelfTextView);
        barcode = (TextView) findViewById(R.id.deweyTextView);
        status = (TextView) findViewById(R.id.statusTextView);
        isTagged = (TextView) findViewById(R.id.taggedTextView);


        //set the book details
        title.setText(books.get(pos).getName().toString());
        author.setText(books.get(pos).getAuthor().toString());
        publisher.setText(books.get(pos).getPublisher().toString());
        year.setText(books.get(pos).getYear().toString());
        shelf.setText(books.get(pos).getShelf().toString());
        barcode.setText(books.get(pos).getBarcode().toString());
        bar = books.get(pos).getBarcode().toString();
        isTagged.setText(books.get(pos).getTagged().toString());

        // add button listener
        getDirectionsBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //call async task for book position
               new GetBookSector().execute();
            }
        });

        borrowBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call async task for borrow
                //new UpdateBorrow().execute();
                //Intent intent = new Intent(context, BookBorrowActivity.class);
                //startActivity(intent);
                borrowBtnClicked=1;
                borrowDialog = new Dialog(context);
                borrowDialog.setContentView(R.layout.dialog_write);
                borrowDialog.setTitle("Scan a Book Tag");
                Button dialogButton = (Button) borrowDialog.findViewById(R.id.write_cancel_button);
                // if button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        borrowDialog.dismiss();
                        borrowBtnClicked=0;
                    }
                });
                borrowDialog.show();
            }
        });

        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alreadyAddedFlag == 0) {
                    //add book to cart
                    FileOutputStream fos = null;
                    try {
                        fos = openFileOutput(FILENAME, Context.MODE_APPEND);
                        fos.write(barcode.getText().toString().getBytes());
                        fos.write(System.getProperty("line.separator").getBytes());
                        fos.close();
                        Toast.makeText(context, "Book " + bk.getBarcode().toString() + " Added to Cart", Toast.LENGTH_SHORT).show();
                        alreadyAddedFlag = 1;

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else Toast.makeText(context,"Book is Already in Cart",Toast.LENGTH_SHORT).show();
            }
        });

        if(books.get(pos).getTagged().toString().equals("Yes")) {
            isTagged.setText("Book is tagged");
            isTagged.setTextColor(getResources().getColor(R.color.emerald));

            if(books.get(pos).getStatus().toString().equals("ok")) {
                status.setText("Book exists on shelf");
                status.setTextColor(getResources().getColor(R.color.emerald));
            }
            else {
                isTagged.setText("Book is already borrowed");
                isTagged.setTextColor(getResources().getColor(R.color.reddd));
                getDirectionsBtn.setClickable(false);
                getDirectionsBtn.setBackgroundColor(getResources().getColor(R.color.mid_blue));
                borrowBookBtn.setClickable(false);
                borrowBookBtn.setBackgroundColor(getResources().getColor(R.color.mid_blue));
                addToCartBtn.setClickable(false);
                addToCartBtn.setBackgroundColor(getResources().getColor(R.color.mid_blue));
            }
        }
        else {
            isTagged.setText("Book is not tagged");
            isTagged.setTextColor(getResources().getColor(R.color.reddd));
            getDirectionsBtn.setClickable(false);
            getDirectionsBtn.setBackgroundColor(getResources().getColor(R.color.mid_blue));
            borrowBookBtn.setClickable(false);
            borrowBookBtn.setBackgroundColor(getResources().getColor(R.color.mid_blue));
            addToCartBtn.setClickable(false);
            addToCartBtn.setBackgroundColor(getResources().getColor(R.color.mid_blue));
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        handleIntent(getIntent());
    }

    protected void onResume() {
        super.onResume();

		/*
		 * It's important, that the activity is in the foreground (resumed). Otherwise
		 * an IllegalStateException is thrown.
		 */

        new GetBookDetails().execute();
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

            if (borrowBtnClicked == 1) {
                if (result != null) {
                    String type = result.substring(0, 2);
                    //int row = Integer.parseInt(result.substring(2,4));

                    if (type.equals("BK")) {
                        bar = result.substring(2);
                        if(bar.equals(bk.getBarcode().toString())) {
                            super.onPostExecute(result);
                            new UpdateBorrow().execute();
                        }
                        else Toast.makeText(context, "Scan Appropriate Book Tag", Toast.LENGTH_SHORT).show();
                    } else Toast.makeText(context, "Please Scan a Book/Shelf/User Tag Only", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class UpdateBorrow extends AsyncTask<String, String, String> {

        /* *
          * Getting product details in background thread
          **/
        protected String doInBackground(String... params) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;
                    try{
                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                            params.add(new BasicNameValuePair("barcode", bar ));

                            // getting student details by making HTTP request
                            // Note that product details url will use GET request
                            JSONObject json = jsonParser.makeHttpRequest(
                                    url_book_borrow, "GET", params);

                            // json success tag
                            if(json!=null) {
                                success = json.getInt(TAG_SUCCESS);
                                if (success == 1) {
                                    successFlag=1;

                                } else {
                                    Toast.makeText(context,"Please Try Again", Toast.LENGTH_SHORT).show();
                                    // product with pid not found
                                }
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }


                    if(successFlag==1){
                        directDialog = new Dialog(context);
                        directDialog.setContentView(R.layout.direction_dialog);
                        directDialog.setTitle("Success");
                        TextView bookCase = (TextView) directDialog.findViewById(R.id.textBC);
                        TextView shelff = (TextView) directDialog.findViewById(R.id.textShelf);
                        bookCase.setText("");
                        shelff.setText("Book Successfully Borrowed");
                        ImageView image = (ImageView) directDialog.findViewById(R.id.directImage);
                        image.setImageResource(R.drawable.success);
                        Button dialogButtonCancel = (Button) directDialog.findViewById(R.id.directionButtonCancel);
                        // if button is clicked, close the custom dialog
                        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new GetBookDetails().execute();
                                directDialog.dismiss();
                            }
                        });
                        directDialog.show();
                        borrowDialog.dismiss();
                    }


                }
            });

            return null;
        }
    }

    class GetBookSector extends AsyncTask<String, String, String> {

        /**
         * Getting book position details in background thread
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
                        params.add(new BasicNameValuePair("barcode", bk.getBarcode().toString()));

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
                                int stand = product.getInt("stand");
                                int shelf = product.getInt("shelf");

                                // custom dialog
                                directDialog = new Dialog(context);
                                directDialog.setContentView(R.layout.direction_dialog);
                                directDialog.setTitle("Directions");
                                TextView bookCase = (TextView) directDialog.findViewById(R.id.textBC);
                                TextView shelff = (TextView) directDialog.findViewById(R.id.textShelf);

                                // fill according to bookcase location
                                /*if(stand==1 || stand==2) bookCase.setText("First BookCase on the left");
                                if(stand==3 || stand==4) bookCase.setText("Second BookCase on the left");
                                if(stand==5 || stand==6) bookCase.setText("Third BookCase on the left");
                                if(stand==7 || stand==8) bookCase.setText("Fourth BookCase on the left");
                                if(stand==9 || stand==10) bookCase.setText("Fifth BookCase on the left");
                                if(stand==11 || stand==12) bookCase.setText("Sixth BookCase on the left");
                                if(stand==13 || stand==14) bookCase.setText("First BookCase on the right");
                                if(stand==15 || stand==16) bookCase.setText("Second BookCase on the right");
                                if(stand==17 || stand==18) bookCase.setText("Third BookCase on the right");
                                if(stand==19 || stand==20) bookCase.setText("Fourth BookCase on the right");
                                if(stand==21 || stand==22) bookCase.setText("Fifth BookCase on the right");
                                if(stand==23 || stand==24) bookCase.setText("Sixth BookCase on the right");*/
                                if(stand==1) bookCase.setText("BookCase #2");
                                if(stand==2) bookCase.setText("BookCase #3");
                                if(stand==3) bookCase.setText("BookCase #4");
                                if(stand==4) bookCase.setText("BookCase #5");
                                if(stand==5) bookCase.setText("BookCase #6");
                                if(stand==6) bookCase.setText("BookCase #7");
                                if(stand==7) bookCase.setText("BookCase #8");
                                if(stand==8) bookCase.setText("BookCase #9");
                                if(stand==9) bookCase.setText("BookCase #10");
                                if(stand==10) bookCase.setText("BookCase #11");
                                if(stand==11) bookCase.setText("BookCase #13");
                                if(stand==12) bookCase.setText("BookCase #14");
                                if(stand==13) bookCase.setText("BookCase #33");
                                if(stand==14) bookCase.setText("BookCase #34");
                                if(stand==15) bookCase.setText("BookCase #35");
                                if(stand==16) bookCase.setText("BookCase #36");
                                if(stand==17) bookCase.setText("BookCase #37");
                                if(stand==18) bookCase.setText("BookCase #38");
                                if(stand==19) bookCase.setText("BookCase #39");
                                if(stand==20) bookCase.setText("BookCase #40");
                                if(stand==21) bookCase.setText("BookCase #41");
                                if(stand==22) bookCase.setText("BookCase #42");
                                if(stand==23) bookCase.setText("BookCase #43");
                                if(stand==24) bookCase.setText("BookCase #44");


                                if(shelf==1) shelff.setText("Top Shelf");
                                if(shelf==2) shelff.setText("2nd Shelf from top");
                                if(shelf==3) shelff.setText("3rd Shelf from top");
                                if(shelf==4) shelff.setText("4th Shelf from top");
                                if(shelf==5) shelff.setText("Bottom Shelf");

                                ImageView image = (ImageView) directDialog.findViewById(R.id.directImage);
                                //change source according to book location
                                if(stand%2==0) {
                                    if(sector==1) image.setImageResource(R.drawable.sector_five);
                                    if(sector==2) image.setImageResource(R.drawable.sector_six);
                                    if(sector==3) image.setImageResource(R.drawable.sector_seven);
                                    if(sector==4) image.setImageResource(R.drawable.sector_eight);

                                }
                                else {
                                    if(sector==1) image.setImageResource(R.drawable.sector_one);
                                    if(sector==2) image.setImageResource(R.drawable.sector_two);
                                    if(sector==3) image.setImageResource(R.drawable.sector_three);
                                    if(sector==4) image.setImageResource(R.drawable.sector_four);
                                }

                                Button dialogButtonCancel = (Button) directDialog.findViewById(R.id.directionButtonCancel);
                                Button dialogButtonScan = (Button) directDialog.findViewById(R.id.directionButtonScan);
                                dialogButtonScan.setVisibility(View.VISIBLE);


                                // if button is clicked, close the custom dialog
                                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        directDialog.dismiss();
                                    }
                                });

                                dialogButtonScan.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(SearchResultActivity.this, ScanShelfActivity.class);
                                        intent.putExtra("barcode", bk.getBarcode().toString());
                                        startActivity(intent);
                                    }
                                });

                                directDialog.show();


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
                                if(product.getString("status").equals("ok")) {
                                    status.setText("Book exists on shelf");
                                    status.setTextColor(getResources().getColor(R.color.emerald));
                                }
                                else {
                                    status.setText("Book is already borrowed");
                                    status.setTextColor(getResources().getColor(R.color.reddd));
                                    getDirectionsBtn.setClickable(false);
                                    getDirectionsBtn.setBackgroundColor(getResources().getColor(R.color.mid_blue));
                                    borrowBookBtn.setClickable(false);
                                    borrowBookBtn.setBackgroundColor(getResources().getColor(R.color.mid_blue));
                                    addToCartBtn.setClickable(false);
                                    addToCartBtn.setBackgroundColor(getResources().getColor(R.color.mid_blue));
                                }

                            } else {
                                Toast.makeText(context, "Error: No Book Details ", Toast.LENGTH_SHORT).show();
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
}
