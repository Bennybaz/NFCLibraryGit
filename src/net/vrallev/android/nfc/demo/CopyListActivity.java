package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lidor on 13/04/14.
 */
public class CopyListActivity extends Activity {

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCT = "reader";

    Context context;
    ArrayList<Book> books;
    ArrayList<Book> copy = new ArrayList<Book>();
    int pos;
    Book bk;
    String bookID;

    ListView lv;
    public CopyResultsAdapter adapter;

    private static final String url_copy_details = "http://nfclibrary.site40.net/book_copy_test.php";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.copy_list);
        setTitle("Copy List");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        context = this;
        books =  getIntent().getParcelableArrayListExtra("bookList");
        pos =  getIntent().getIntExtra("position",0);
        bk = books.get(pos);
        bookID = bk.getBookID();

        /*for(int i=0; i<5 ;i++)
        {
            Book b = new Book();
            b.setName("lol"+i);
            b.setAuthor("hh"+i);
            copy.add(b);
        }*/

        lv = (ListView) findViewById(R.id.copyList);
        adapter = new CopyResultsAdapter(this, copy);
        lv.setAdapter(adapter);

        new GetSearchResults().execute();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Intent intent2 = new Intent(SearchResultsActivity.this, SearchResultActivity.class);
                Intent intent2 = new Intent(CopyListActivity.this, SearchResultActivity.class);
                intent2.putParcelableArrayListExtra("bookList", copy);
                intent2.putExtra("position", position);
                startActivity(intent2);
            }
        });

    }

    class GetSearchResults extends AsyncTask<String, String, String> {

        /**
         * Getting product details in background thread
         * */
        protected String doInBackground(String... params) {
            //updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;

                    try {
                        // Building Parameters

                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("bookID", bookID));

                        // getting student details by making HTTP request
                        // Note that product details url will use GET request
                        JSONObject json = jsonParser.makeHttpRequest(
                                url_copy_details, "GET", params);

                        //Toast.makeText(context, json.toString(), Toast.LENGTH_LONG).show();
                        // check your log for json response
                        //Log.d("Single Product Details", json.toString());

                        // json success tag
                        if(json!=null) {
                            success = json.getInt(TAG_SUCCESS);
                            if (success == 1) {

                                // successfully received product details
                                JSONArray productObj = json
                                        .getJSONArray("books"); // JSON Array



                                for(int i=0; i<productObj.length(); i++)
                                {
                                    // get first user object from JSON Array
                                    JSONObject product = productObj.getJSONObject(i);
                                    Book b = new Book();
                                    b.setBookID(bookID);
                                    b.setLocation(bk.getLocation());
                                    b.setShelf(bk.getShelf());
                                    b.setBarcode(product.getString("barcode"));
                                    b.setAuthor(bk.getAuthor());
                                    b.setName(bk.getName());
                                    b.setYear(bk.getYear());
                                    b.setPublisher(bk.getPublisher());
                                    b.setStatus(product.getString("status"));
                                    copy.add(b);
                                    adapter.notifyDataSetChanged();
                                }

                            } else {
                                // product with pid not found
                                Toast.makeText(context, "No Such Item", Toast.LENGTH_LONG).show();
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