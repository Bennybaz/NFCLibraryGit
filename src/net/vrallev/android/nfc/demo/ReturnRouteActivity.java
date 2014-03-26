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
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.*;
import libalg.BranchAndBound;

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

    //*********
    //private ListView lv;
    //private ArrayList<String> strArr;
    //private ArrayAdapter<String> adapter;
    public MySimpleArrayAdapter adapter;
    ArrayList<Book> b = new ArrayList<Book>();
    //********

    Button calcBtn;
    private NfcAdapter mNfcAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.return_route);
        context = this;

        calcBtn = (Button) findViewById(R.id.calcRouteBtn);
        lv = (ListView) findViewById(R.id.listView);

        //*******
        //lv = (ListView) findViewById(R.id.listView);

        //adapter = new ArrayAdapter<String>(getApplicationContext(),
        //        android.R.layout.simple_list_item_1, strArr);

        //lv.setAdapter(adapter);
        //Book [] books = new Book[30];
        //books[1]=new Book("SQL","Dani");
       /* Book check = new Book("SQL","Dani");
        b.add(check);*/
        for(int i=1; i<10; i++)
        {
            Book check = new Book(""+i,"des:"+i);
            b.add(check);
        }
        adapter = new MySimpleArrayAdapter(this, b);
        lv.setAdapter(adapter);
        //lv.setListAdapter(adapter);
        //*******

        calcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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



                    //System.out.println(result);




            }
        });
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        handleIntent(getIntent());
    }

    public void createAdj(double[][] input, ArrayList<Integer> sectors){

        double[] row = new double[input.length];
        int i=0;
        //Toast.makeText(context,input.length,Toast.LENGTH_LONG).show();

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

    /*public double[][] getDoubleTwoDimArray(Reader baseReader) throws IOException {
        BufferedReader reader = new BufferedReader(baseReader);
        int rows = Integer.parseInt(reader.readLine());
        int cols = Integer.parseInt(reader.readLine());
        double[][] data = new double[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                data[row][col] = Double.parseDouble(reader.readLine());
            }
        }
        return data;

    }*/

    public double[][] getDoubleTwoDimArray(String fileName) throws IOException {
        /*Reader reader = new FileReader(fileName);

        double[][] data = getDoubleTwoDimArray(reader);
        reader.close();*/

        double[][] data = new double[0][0];

        InputStream databaseInputStream = getResources().openRawResource(R.raw.dump);
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(databaseInputStream));
        int rows = Integer.parseInt(reader.readLine().toString());
        int cols = Integer.parseInt(reader.readLine().toString());


           /* int rows = Integer.parseInt(reader.readLine());
            int cols = Integer.parseInt(reader.readLine());*/
            data = new double[rows][cols];
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    data[row][col] = Double.parseDouble(reader.readLine().toString());
                }
            }
            databaseInputStream.close();
        String result = sb.toString();
        //Toast.makeText(context,result.substring(1,10).toString(),Toast.LENGTH_LONG).show();



//Read text from file
        //StringBuilder text = new StringBuilder();



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
                String type = result.substring(0,1);
                int row = Integer.parseInt(result.substring(1,3));

                if(type.equals("E")){
                    super.onPostExecute(result);

                        if(!sectors.contains(row)){
                            sectors.add(row);
                            //Book book= new Book(type,""+row);
                            //update the book array here
                            //b.add(book);
                            //adapter.notifyDataSetChanged();

                            //strArr.add(result);
                            //adapter.notifyDataSetChanged();
                        }
                }

            }
            Toast.makeText(context,sectors.toString(),Toast.LENGTH_LONG).show();
        }
    }
}
