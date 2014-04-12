package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import libalg.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benny on 17/03/14.
 */
public class StudentMainActivity extends Activity {

    private Button searchBtn;
    private Button borrowBtn;
    private Button cartBtn;
    private TextView greeting;
    private String studentID;
    private Context context;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // username in db url
    private static final String url_user_name_details = "http://nfclibrary.site40.net/get_product_details.php";
    //private static final String url_user_name_details = "http://192.168.2.101/nfc_library/get_product_details.php";


    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCT = "reader";
    //private static final String TAG_PID = "sid";
    private static final String TAG_NAME = "name";

    public void onCreate(Bundle savedInstanceState) {
        context = this;
        setTitle("Reader Menu");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        searchBtn = (Button) findViewById(R.id.searchBtn);
        borrowBtn = (Button) findViewById(R.id.readerBorrowBtn);
        cartBtn = (Button) findViewById(R.id.cartBtn);
        greeting = (TextView) findViewById(R.id.greetText);

        studentID = getIntent().getExtras().getString("ID").substring(2);

        // Getting complete user details in background thread
        new GetUserDetails().execute();




        greeting.setText("Hello, "+studentID);

        searchBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, SearchBook.class);
                intent.putExtra("ID", studentID);
                startActivity(intent);
            }

        });

        borrowBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookBorrowActivity.class);
                intent.putExtra("ID", studentID);
                startActivity(intent);
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookCartActivity.class);
                intent.putExtra("ID", studentID);
                startActivity(intent);
            }
        });
    }

    /**
     * Background Async Task to Get complete product details
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
                        params.add(new BasicNameValuePair("id", studentID));

                        // getting student details by making HTTP request
                        // Note that product details url will use GET request
                        JSONObject json = jsonParser.makeHttpRequest(
                                url_user_name_details, "GET", params);


                        // check your log for json response
                        //Log.d("Single Product Details", json.toString());

                        // json success tag
                        if(json!=null) {
                            success = json.getInt(TAG_SUCCESS);
                            if (success == 1) {

                                // successfully received product details
                                JSONArray productObj = json
                                        .getJSONArray(TAG_PRODUCT); // JSON Array

                                // get first user object from JSON Array
                                JSONObject product = productObj.getJSONObject(0);

                                greeting.setText("Hello, " + product.getString(TAG_NAME));

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
}
