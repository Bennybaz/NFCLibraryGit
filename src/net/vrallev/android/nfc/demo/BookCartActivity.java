package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.*;
import libalg.BranchAndBound;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Benny on 12/04/2014.
 */
public class BookCartActivity extends Activity {

    Context context;
    public ArrayList<String> barcodes  = new ArrayList<String>();
    ListView lv;
    public MySimpleArrayAdapter adapter;
    ArrayList<Book> b = new ArrayList<Book>(); //contains the books for ListView
    int flag = 0;
    Button borrowBtn;
    Dialog directDialog;

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
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setTitle("Cart");
        context = this;

        lv = (ListView) findViewById(R.id.cartListView);
        adapter = new MySimpleArrayAdapter(this, b);
        lv.setAdapter(adapter);
        borrowBtn = (Button) findViewById(R.id.borrowCartBtn);

        //open the file for get the book barcodes from cart
        FileInputStream databaseInputStream = null;
        try {
            databaseInputStream = openFileInput("cart");
            BufferedReader reader = new BufferedReader(new InputStreamReader(databaseInputStream));
            String line=null;

            while((line=reader.readLine())!=null) {

                for (int j = 0; j < barcodes.size(); j++) {
                    if (barcodes.get(j).toString().equals(line.toString()))
                        flag = 1;
                    break;
                }
                if(flag==0) barcodes.add(line);
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
                new UpdateBorrow().execute();
            }
        });




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
                            Toast.makeText(context,barcodes.get(i).toString(),Toast.LENGTH_SHORT).show();
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
                                    directDialog = new Dialog(context);
                                    directDialog.setContentView(R.layout.direction_dialog);
                                    directDialog.setTitle("Success");
                                    TextView bookCase = (TextView) directDialog.findViewById(R.id.textBC);
                                    TextView shelff = (TextView) directDialog.findViewById(R.id.textShelf);
                                    bookCase.setText("");
                                    shelff.setText("Books were borrowed");
                                    ImageView image = (ImageView) directDialog.findViewById(R.id.directImage);
                                    image.setImageResource(success);

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
