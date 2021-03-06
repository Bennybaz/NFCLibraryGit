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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Benny on 12/04/2014.
 */
public class BookCartActivity extends Activity {

    Context context;
    public ArrayList<String> barcodes  = new ArrayList<String>();
    ListView lv;
    public BookCartAdapter adapter;
    ArrayList<Book> b = new ArrayList<Book>(); //contains the books for ListView
    int flag = 0;
    Button borrowBtn;
    Dialog directDialog;
    int successFlag=0;
    int readFlag=0;
    String barcode = "";
    public int bookCount=0;
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private NfcAdapter mNfcAdapter;

    // JSON parser class
    private JSONParser jsonParser = new JSONParser();
    private JSONParser jsonParser2 = new JSONParser();

    // book details in db url
    private static final String url_book_borrow = "http://nfclibrary.site40.net/borrow_book_by_barcode.php";
    private static final String url_book_barcode_for_details = "http://nfclibrary.site40.net/barcode_for_title_and_author.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCT = "book";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_list);

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setTitle("Book Cart");

        context = this;

        lv = (ListView) findViewById(R.id.cartListView);
        adapter = new BookCartAdapter(this, b);
        lv.setAdapter(adapter);

        borrowBtn = (Button) findViewById(R.id.borrowCartBtn);


        //open the file for get the book barcodes from cart
        FileInputStream databaseInputStream = null;
        try {
            databaseInputStream = openFileInput("cart");
            BufferedReader reader = new BufferedReader(new InputStreamReader(databaseInputStream));
            String line=null;

            while((line=reader.readLine())!=null) {
                flag=0;
                for (int j = 0; j < barcodes.size(); j++) {
                    if (barcodes.get(j).toString().equals(line.toString()))
                        flag = 1;
                }
                if(flag==0) {
                    barcodes.add(line);
                }
            }
            databaseInputStream.close();

            new GetBookBarcode().execute();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        borrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (b.size()>0){
                    if (bookCount==b.size()) new UpdateBorrow().execute();
                    else Toast.makeText(context,"Please Scan All Books",Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(context,"No Books in Cart",Toast.LENGTH_SHORT).show();


            }
        });


        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        handleIntent(getIntent());

    }
    //async task for get the book details for view on ListView
    class GetBookBarcode extends AsyncTask<String, String, String> {

        /**
         * Getting books details in background thread
         * */
        protected String doInBackground(String... params) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;

                    for (int i = 0; i < barcodes.size(); i++) {
                        try {
                            // Building Parameters
                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                            params.add(new BasicNameValuePair("barcode", barcodes.get(i).toString()));


                            // getting student details by making HTTP request
                            // Note that product details url will use GET request

                            JSONObject json2 = jsonParser.makeHttpRequest(
                                    url_book_barcode_for_details, "GET", params);



                            // json success tag
                            if (json2 != null) {
                                success = json2.getInt(TAG_SUCCESS);
                                if (success == 1) {
                                    // successfully received product details
                                    JSONArray productObj = json2.getJSONArray(TAG_PRODUCT); // JSON Array

                                    // get first user object from JSON Array
                                    JSONObject product = productObj.getJSONObject(0);

                                    Book bk = new Book();
                                    bk.setBarcode(barcodes.get(i));
                                    bk.setName(product.getString("title"));
                                    bk.setAuthor(product.getString("author"));

                                        b.add(bk);
                                        adapter.notifyDataSetChanged();

                                } else {
                                    // product with pid not found
                                    Toast.makeText(context,"Book doesn't Exist", Toast.LENGTH_SHORT).show();
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

    class UpdateBorrow extends AsyncTask<String, String, String> {

        /* *
          * Updating the book status in background thread
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
                            JSONObject json = jsonParser2.makeHttpRequest(
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

                    }
                    if(successFlag==1) {
                        directDialog = new Dialog(context);
                        directDialog.setContentView(R.layout.direction_dialog);
                        directDialog.setTitle("Success");
                        TextView bookCase = (TextView) directDialog.findViewById(R.id.textBC);
                        TextView shelff = (TextView) directDialog.findViewById(R.id.textShelf);
                        bookCase.setText("");
                        shelff.setText("Books were borrowed");
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
                        getApplicationContext().deleteFile("cart");
                    }
                }
            });

            return null;
        }
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
     * @param activity The corresponding {@link //BaseActivity} requesting to stop the foreground dispatch.
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

                if (b.size() > 0) {
                    if (type.equals("BK")) {
                        super.onPostExecute(result);
                        String barcode2 = result.substring(2);
                        int pos = -1;
                        for (int i = 0; i < b.size(); i++) {
                            readFlag = 0;
                            if (barcode2.equals(adapter.getItem(i).getBarcode())) {
                                bookCount++;
                                pos = i;
                                break;
                            } else {
                                readFlag = 1;
                            }
                        }

                        if (readFlag == 0)
                            lv.getChildAt(pos).setBackgroundColor(getResources().getColor(R.color.emerald));
                        else Toast.makeText(context, "Book doesn't Belong To Cart", Toast.LENGTH_SHORT).show();
                    } else Toast.makeText(context, "Scan a Book Tag Only", Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(context, "Cart is Empty", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class BookCartAdapter extends ArrayAdapter<Book> {
        private Context context;
        public ArrayList<Book> values;

        public BookCartAdapter(Context context, ArrayList<Book> values)
        {
            super(context, R.layout.row_layout, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.row_layout, parent, false);
            TextView textView1 = (TextView) rowView.findViewById(R.id.headline);
            TextView textView2 = (TextView) rowView.findViewById(R.id.baseline);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.item_image_right);
            textView1.setText(values.get(position).getName());
            textView2.setText(values.get(position).getAuthor()+", Barcode:"+values.get(position).getBarcode());
            imageView.setImageResource(R.drawable.remove_icon);
            imageView.setFocusable(false);
            imageView.setFocusableInTouchMode(false);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    values.remove(pos);
                    notifyDataSetChanged();
                    bookCount--;
                }
            });
            return rowView;
        }

    }
}
