package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benny on 21/03/2014.
 */
public class LibrarianMainActivity extends Activity {

    private Button searchBtn;
    private Button returnBtn;
    private Button manageBtn;
    private TextView greeting;
    private String librarianID;
    private Context context;
    private Button borrowBtn;

    // JSON parser class
    private JSONParser jsonParser = new JSONParser();

    // username in db url
    private static final String url_user_name_details = "http://nfclibrary.site40.net/get_product_details.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCT = "reader";
    //private static final String TAG_PID = "sid";
    private static final String TAG_NAME = "name";

    public void onCreate(Bundle savedInstanceState) {
        context = this;
        setTitle("Librarian Menu");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.librarian_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        searchBtn = (Button) findViewById(R.id.searchBtn);
        returnBtn = (Button) findViewById(R.id.bookReturnBtn);
        manageBtn = (Button) findViewById(R.id.manageBtn);
        borrowBtn = (Button) findViewById(R.id.libBorrow);
        greeting = (TextView) findViewById(R.id.greetText2);

        //get the librarian id
        //librarianID = getIntent().getExtras().getString("ID").substring(2);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        librarianID = sharedPref.getString("ID", "OOPS").substring(2);


        // Getting complete user details in background thread
        GetUserDetails task1 = new GetUserDetails();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task1.execute();

        greeting.setText("Hello, "+librarianID);

        searchBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, SearchBook.class);
                intent.putExtra("ID", librarianID);
                startActivity(intent);
            }

        });

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LibrarianMainActivity.this, ReturnRouteActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        manageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ManageInventoryActivity.class);
                intent.putExtra("ID", librarianID);
                startActivity(intent);
            }
        });

        borrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookBorrowActivity.class);
                intent.putExtra("ID", librarianID);
                startActivity(intent);
            }
        });
    }

    //Background Async Task to Get complete user details
    class GetUserDetails extends AsyncTask<String, String, String> {

        /**
         * Getting librarian details in background thread
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
                        params.add(new BasicNameValuePair("id", librarianID));

                        // getting student details by making HTTP request
                        // Note that product details url will use GET request
                        JSONObject json = jsonParser.makeHttpRequest(
                                url_user_name_details, "GET", params);

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
                                // librarian with id not found
                            }
                        }
                        else {

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
