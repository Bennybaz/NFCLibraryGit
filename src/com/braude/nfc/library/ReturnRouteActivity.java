package com.braude.nfc.library;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
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
import android.widget.Toast;
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
    ArrayList<Integer> optimumRoute = new ArrayList<Integer>();
    HashMap<Double, ArrayList<String>> barcodeSector = new HashMap<Double, ArrayList<String>>(); //added for sorting
    ArrayList<Book> sorted = new ArrayList<Book>();
    ProgressDialog pDialog;

    ArrayList<String> sortCommands = new ArrayList<String>();

    Button calcBtn;
    Button simulationBtn;
    private NfcAdapter mNfcAdapter;
    String barcode;
    int flag=0;
    double fixedPos=0;
    double it;
    int firstSectorFlag = 0; //flag for the returnRoute array size
    int simulationFlag=0; //flag for simulation, running or not
    int duplicateFlag=0;

    int shelf;
    int sector;
    int stand;

    // JSON parser class
    private JSONParser jsonParser = new JSONParser();
    private JSONParser jsonParser2 = new JSONParser();
    private JSONParser jsonParser3 = new JSONParser();
    private JSONParser jsonParser4 = new JSONParser();

    // username in db url
    private static final String url_book_barcode_for_details = "http://nfclibrary.site40.net/barcode_for_title_and_author.php";
    private static final String url_book_barcode_for_sector = "http://nfclibrary.site40.net/barcode_to_sector.php";
    private static final String url_return_book_by_barcode = "http://nfclibrary.site40.net/return_book_by_barcode.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCT = "book";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.return_route);
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setTitle("Return Books");
        context = this;

        calcBtn = (Button) findViewById(R.id.calcRouteBtn);
        simulationBtn = (Button) findViewById(R.id.simulationButton);
        lv = (ListView) findViewById(R.id.listView);
        adapter = new MySimpleArrayAdapter(this, b);
        lv.setAdapter(adapter);

        simulationFlag=0;

        simulationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(simulationFlag==0)
                {
                    new GetBookSectorForSimulation2().execute();
                    simulationFlag=1;
                }
                else Toast.makeText(context,"Simulation is already running",Toast.LENGTH_SHORT).show();
            }
        });


        calcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(b.size()>0)
                {

                    //update the book return on DB
                    //new UpdateBookStatus().execute();
                    try {
                        double[][] data2 = getDoubleTwoDimArray("dump.txt");
                        firstSectorFlag=0;
                        if(sectors.get(0)!=1)
                        {
                            sectors.add(1);
                            firstSectorFlag=1;
                        }

                        Collections.sort(sectors);
                        adjacency_matrix = new double[sectors.size() + 1][sectors.size() + 1];

                        createAdj(data2, sectors);

                        BranchAndBound bnb = new BranchAndBound(adjacency_matrix,0, sectors);
                        //String result = bnb.execute();
                        optimumRoute = bnb.execute2();

                        sortCommands.clear();
                        sorted.clear();
                        new GetBookBarcodeSorted().execute();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else Toast.makeText(context,"No Books For Return",Toast.LENGTH_SHORT).show();
            }

        });
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        handleIntent(getIntent());
    }

    public int bookPosInScannedBooks(String str, ArrayList<Book> al)
    {
        for(int i=0; i<al.size(); i++)
        {
            if(al.get(i).getBarcode().equals(str))
            {
                al.remove(i);
                return i;
            }

        }
        return -1;
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

            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(300);
            if (result != null) {
                String type = result.substring(0,2);
                barcode=result.substring(2);

                if(type.equals("BK")){
                    super.onPostExecute(result);
                    int flagg=0;
                    for(int i=0; i<b.size(); i++) {
                        if (b.get(i).getBarcode().equals(barcode)) flagg = 1;
                    }

                    if(flagg==1) Toast.makeText(context, "Book Already Exists", Toast.LENGTH_SHORT).show();
                        else new GetBookSector().execute();


                }
                else Toast.makeText(context,"Scan a Book Tag Only",Toast.LENGTH_SHORT).show();

            }
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

                                shelf = product.getInt("shelf");
                                sector = product.getInt("sector");
                                stand = product.getInt("stand");

                                Integer sectorForAlg = (stand-1)*4+sector;
                                fixedPos = sectorForAlg+shelf*0.1;


                                if(!sectors.contains(sectorForAlg)) {
                                    sectors.add(sectorForAlg);
                                }

                                ArrayList<String> tempBar;
                                tempBar = barcodeSector.get(fixedPos);
                                if(tempBar == null)
                                    tempBar = new ArrayList<String>();
                                tempBar.add(barcode);
                                barcodeSector.put(fixedPos,tempBar);

                                try {
                                    // Building Parameters
                                    List<NameValuePair> params1 = new ArrayList<NameValuePair>();
                                    params1.add(new BasicNameValuePair("barcode", barcode));

                                    // getting student details by making HTTP request
                                    // Note that product details url will use GET request

                                    JSONObject json3 = jsonParser.makeHttpRequest(
                                            url_book_barcode_for_details, "GET", params);

                                    // json success tag
                                    if(json3!=null) {
                                        success = json3.getInt(TAG_SUCCESS);
                                        if (success == 1) {
                                            // successfully received product details
                                            JSONArray productObj1 = json3.getJSONArray(TAG_PRODUCT); // JSON Array

                                            // get first user object from JSON Array
                                            JSONObject product1 = productObj1.getJSONObject(0);

                                            Book bk = new Book();
                                            bk.setBarcode(barcode);
                                            bk.setName(product1.getString("title"));
                                            bk.setAuthor(product1.getString("author"));
                                            bk.setFixedPosition(fixedPos);


                                            for(int i=0; i<b.size(); i++) {
                                                if(b.get(i).getBarcode().equals(barcode))
                                                    flag=1;
                                                break;
                                            }

                                            if(flag==0) {
                                                b.add(bk);
                                                adapter.notifyDataSetChanged();
                                                lv.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // Select the last row so it will scroll into view...
                                                        lv.setSelection(lv.getCount() - 1);
                                                    }
                                                });
                                            }
                                            else Toast.makeText(context,"Book Already Exists", Toast.LENGTH_SHORT).show();

                                        } else {
                                            Toast.makeText(context,"Error: cannot find the book details",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                Toast.makeText(context,"Error: cannot find the book location",Toast.LENGTH_SHORT).show();
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
                    ArrayList<Book> tempBooks = new ArrayList<Book>(b);
                    for(int i=0; i< tempBooks.size(); i++)
                    {
                        try{
                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                            params.add(new BasicNameValuePair("barcode", tempBooks.get(i).getBarcode().toString()));

                            // getting student details by making HTTP request
                            // Note that product details url will use GET request
                            JSONObject json = jsonParser3.makeHttpRequest(
                                    url_return_book_by_barcode, "GET", params);

                            // json success tag
                            if(json!=null) {
                                success = json.getInt(TAG_SUCCESS);
                                if (success == 1) {
                                    // successfully received product details
                                    //JSONArray productObj = json
                                    //       .getJSONArray(TAG_PRODUCT); // JSON Array

                                    // get first user object from JSON Array
                                    //JSONObject product = productObj.getJSONObject(0);
                                    Toast.makeText(context,"Books Status Changed",Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(context,"Book doesn't Exist",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    tempBooks.clear();
                }
            });

            return null;
        }
    }

    class GetBookBarcodeSorted extends AsyncTask<String, String, String> {



        /**
         * Getting product details in background thread
         * */
        protected String doInBackground(String... params) {



            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;

                    int i;
                    if(firstSectorFlag==1)
                        i=1;
                    else
                        i=0;
                    for (   ; i< optimumRoute.size()-1; i++)
                    {
                        int item=optimumRoute.get(i);
                        for(int j=1; j<=5; j++)
                        {
                            int s=0;
                            it = (double)item+j*0.1;
                            ArrayList<String> barcodeList = new ArrayList<String>();
                            barcodeList = barcodeSector.get(it);

                            if(barcodeList != null)
                            {
                                while(s<barcodeList.size())
                                {
                                    barcode = barcodeList.get(s);
                                    s++;
                                    try {
                                        // Building Parameters
                                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                                        params.add(new BasicNameValuePair("barcode", barcode));
                                        // getting student details by making HTTP request
                                        // Note that product details url will use GET request
                                        JSONObject json4 = jsonParser4.makeHttpRequest(
                                                url_book_barcode_for_details, "GET", params);

                                        // json success tag
                                        if(json4!=null) {
                                            success = json4.getInt(TAG_SUCCESS);
                                            if (success == 1) {
                                                // successfully received product details
                                                JSONArray productObj = json4.getJSONArray(TAG_PRODUCT); // JSON Array

                                                // get first user object from JSON Array
                                                JSONObject product = productObj.getJSONObject(0);

                                                Book bk = new Book();
                                                bk.setBarcode(barcode);
                                                bk.setName(product.getString("title"));
                                                bk.setAuthor(product.getString("author"));
                                                bk.setFixedPosition(it);

                                                int flag1=0;
                                                for(int k=0; k<sorted.size() && flag==0; k++)
                                                    if(sorted.get(k).getBarcode().equals(barcode))
                                                        flag1=1;
                                                if(flag1==0)
                                                {
                                                    sorted.add(bk);
                                                }

                                            } else {
                                                Toast.makeText(context,"Error: cannot find the book details for sort",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                    }//end for

                    ArrayList<Book> unsorted = new ArrayList<Book>(b);

                    int sameSectorFlag=0;

                    for(int j=0; j<b.size()-1; j++)
                    {
                        int num1=(int) b.get(j).getFixedPosition().doubleValue();
                        int num2=(int) b.get(j+1).getFixedPosition().doubleValue();
                        if(num1!=num2)
                            sameSectorFlag=1;
                    }

                    if(sameSectorFlag==1)
                    {
                        for(int j=0; j<sorted.size(); j++)
                        {
                            int bookPos = bookPosInScannedBooks(sorted.get(j).getBarcode(), unsorted);
                            if(bookPos==-1)
                                Toast.makeText(context,"Sorting Error",Toast.LENGTH_SHORT).show();
                            String msg = new String("Put Book #"+(bookPos+1)+" in position #"+(j+1)+" of the sorted pile");
                            sortCommands.add(msg);
                        }

                        Intent intent = new Intent(ReturnRouteActivity.this, SortedList.class);
                        intent.putParcelableArrayListExtra("books",sorted);
                        intent.putStringArrayListExtra("srtCmd", sortCommands);

                        intent.putExtra("SameFlag", sameSectorFlag);
                        startActivity(intent);
                    }
                    else
                    {
                        Intent intent = new Intent(ReturnRouteActivity.this, ReturnRouteInstructions.class);
                        intent.putParcelableArrayListExtra("books",sorted);

                        intent.putExtra("SameFlag", sameSectorFlag);
                        startActivity(intent);
                    }
                }

            });
            return null;
        }


    }


    class GetBookSectorForSimulation extends AsyncTask<String, String, String> {

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

                    simBarcode.add("1568-20");
                    simBarcode.add("624-10");
                    simBarcode.add("1298-10");
                    simBarcode.add("602-10");
                    simBarcode.add("1568-10");


                    for(int i=0; i<simBarcode.size(); i++)
                    {
                        barcode = simBarcode.get(i);
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

                                    shelf = product.getInt("shelf");
                                    sector = product.getInt("sector");
                                    stand = product.getInt("stand");

                                    Integer sectorForAlg = (stand-1)*4+sector;
                                    fixedPos = sectorForAlg+shelf*0.1;


                                    if(!sectors.contains(sectorForAlg)) {
                                        sectors.add(sectorForAlg);
                                    }

                                    ArrayList<String> tempBar;
                                    tempBar = barcodeSector.get(fixedPos);
                                    if(tempBar == null)
                                        tempBar = new ArrayList<String>();
                                    tempBar.add(barcode);
                                    barcodeSector.put(fixedPos,tempBar);

                                    try {
                                        // Building Parameters
                                        List<NameValuePair> params1 = new ArrayList<NameValuePair>();
                                        params1.add(new BasicNameValuePair("barcode", barcode));

                                        // getting student details by making HTTP request
                                        // Note that product details url will use GET request

                                        JSONObject json3 = jsonParser.makeHttpRequest(
                                                url_book_barcode_for_details, "GET", params);

                                        // json success tag
                                        if(json3!=null) {
                                            success = json3.getInt(TAG_SUCCESS);
                                            if (success == 1) {
                                                // successfully received product details
                                                JSONArray productObj1 = json3.getJSONArray(TAG_PRODUCT); // JSON Array

                                                // get first user object from JSON Array
                                                JSONObject product1 = productObj1.getJSONObject(0);

                                                Book bk = new Book();
                                                bk.setBarcode(barcode);
                                                bk.setName(product1.getString("title"));
                                                bk.setAuthor(product1.getString("author"));
                                                bk.setFixedPosition(fixedPos);

                                                for(int j=0; j<b.size(); j++) {
                                                    if(b.get(j).getBarcode().equals(barcode))
                                                        flag=1;
                                                    break;
                                                }

                                                if(flag==0) {
                                                    b.add(bk);
                                                    adapter.notifyDataSetChanged();
                                                }

                                            } else {
                                                Toast.makeText(context,"Error: cannot find the book details",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    Toast.makeText(context,"Error: cannot find the book details(location)",Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }//end for
                }
            });

            return null;
        }


    }


    class GetBookSectorForSimulation2 extends AsyncTask<String, String, String> {

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
                    Integer sectorForAlg;
                    ArrayList<String> tempBar;

                    simBarcode.add("1568-20");
                    ///////////
                    barcode="1568-20";
                    shelf = 4;
                    sector = 1;
                    stand = 1;
                    sectorForAlg = (stand-1)*4+sector;
                    fixedPos = sectorForAlg+shelf*0.1;
                    if(!sectors.contains(sectorForAlg)) {
                        sectors.add(sectorForAlg);
                    }
                    tempBar = barcodeSector.get(fixedPos);
                    if(tempBar == null)
                        tempBar = new ArrayList<String>();
                    tempBar.add(barcode);
                    barcodeSector.put(fixedPos,tempBar);
                    Book bk = new Book();
                    bk.setBarcode(barcode);
                    bk.setName("The C++ programming language");
                    bk.setAuthor("Stroustrup, Bjarne");
                    bk.setFixedPosition(fixedPos);
                    for(int j=0; j<b.size(); j++) {
                        if(b.get(j).getBarcode().equals(barcode))
                            flag=1;
                        break;
                    }
                    if(flag==0) {
                        b.add(bk);
                        adapter.notifyDataSetChanged();
                    }
                    ///////////
                    simBarcode.add("624-10");
                    barcode="624-10";
                    shelf = 5;
                    sector = 4;
                    stand = 3;
                    sectorForAlg = (stand-1)*4+sector;
                    fixedPos = sectorForAlg+shelf*0.1;
                    if(!sectors.contains(sectorForAlg)) {
                        sectors.add(sectorForAlg);
                    }
                    tempBar = barcodeSector.get(fixedPos);
                    if(tempBar == null)
                        tempBar = new ArrayList<String>();
                    tempBar.add(barcode);
                    barcodeSector.put(fixedPos,tempBar);
                    Book bk1 = new Book();
                    bk1.setBarcode(barcode);
                    bk1.setName("C programmers guide to serial communications");
                    bk1.setAuthor("Campbell, Joe");
                    bk1.setFixedPosition(fixedPos);
                    for(int j=0; j<b.size(); j++) {
                        if(b.get(j).getBarcode().equals(barcode))
                            flag=1;
                        break;
                    }
                    if(flag==0) {
                        b.add(bk1);
                        adapter.notifyDataSetChanged();
                    }
                    //////////
                    simBarcode.add("1298-10");
                    barcode="1298-10";
                    shelf = 5;
                    sector = 2;
                    stand = 2;
                    sectorForAlg = (stand-1)*4+sector;
                    fixedPos = sectorForAlg+shelf*0.1;
                    if(!sectors.contains(sectorForAlg)) {
                        sectors.add(sectorForAlg);
                    }
                    tempBar = barcodeSector.get(fixedPos);
                    if(tempBar == null)
                        tempBar = new ArrayList<String>();
                    tempBar.add(barcode);
                    barcodeSector.put(fixedPos,tempBar);
                    Book bk2 = new Book();
                    bk2.setBarcode(barcode);
                    bk2.setName("Problem-Solving principles programming with Pascal...");
                    bk2.setAuthor("Prather, Ronald E.");
                    bk2.setFixedPosition(fixedPos);
                    for(int j=0; j<b.size(); j++) {
                        if(b.get(j).getBarcode().equals(barcode))
                            flag=1;
                        break;
                    }
                    if(flag==0) {
                        b.add(bk2);
                        adapter.notifyDataSetChanged();
                    }
                    ////////////
                    simBarcode.add("7249-20");
                    barcode="7249-20";
                    shelf = 1;
                    sector = 1;
                    stand = 2;
                    sectorForAlg = (stand-1)*4+sector;
                    fixedPos = sectorForAlg+shelf*0.1;
                    if(!sectors.contains(sectorForAlg)) {
                        sectors.add(sectorForAlg);
                    }
                    tempBar = barcodeSector.get(fixedPos);
                    if(tempBar == null)
                        tempBar = new ArrayList<String>();
                    tempBar.add(barcode);
                    barcodeSector.put(fixedPos,tempBar);
                    Book bk3 = new Book();
                    bk3.setBarcode(barcode);
                    bk3.setName("Microprocessor pocket book");
                    bk3.setAuthor("Money, Steve A.");
                    bk3.setFixedPosition(fixedPos);
                    for(int j=0; j<b.size(); j++) {
                        if(b.get(j).getBarcode().equals(barcode))
                            flag=1;
                        break;
                    }
                    if(flag==0) {
                        b.add(bk3);
                        adapter.notifyDataSetChanged();
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
            b.clear();
            sorted.clear();
            sectors.clear();
            optimumRoute.clear();
            sortCommands.clear();
            adapter.notifyDataSetChanged();
        }
        return super.onKeyDown(keyCode, event);
    }
}