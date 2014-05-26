package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
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
public class SearchBook extends Activity {

    private Button button;
    private EditText query;
    private Spinner field;
    ArrayList<Book> books = new ArrayList<Book>(); //contains the books(search results)
    Context context;


    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // books in db url
    //private static final String url_user_name_details = "http://nfclibrary.site40.net/search_book_by_author_or_title.php";
    private static final String url_user_name_details = "http://nfclibrary.site40.net/search_book_test.php";


    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    public void onCreate(Bundle savedInstanceState) {
        setTitle("Search Book");
        context=this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_search_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        button = (Button) findViewById(R.id.buttonUrl);
        query = (EditText) findViewById(R.id.queryET);
        field = (Spinner) findViewById(R.id.fieldSpinner);


        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Getting complete search details in background thread
                books.clear();

                new GetSearchResults().execute();
            }

        });
    }


    /**
     * Background Async Task to Get complete search details
     * */
    class GetSearchResults extends AsyncTask<String, String, String> {

        /**
         * Getting search results in background thread
         * */
        protected String doInBackground(String... params) {
            //updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;

                    //type of search
                    String searchField = new String();

                    try {
                        // Building Parameters
                        if(field.getSelectedItemPosition() == 0)
                            searchField = "name";
                        if(field.getSelectedItemPosition() == 1)
                            searchField = "author";

                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("searchField", searchField));
                        params.add(new BasicNameValuePair("keyWord", query.getText().toString()));

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
                                        .getJSONArray("books"); // JSON Array

                                for(int i=0; i<productObj.length(); i++)
                                {
                                    // get first user object from JSON Array
                                    JSONObject product = productObj.getJSONObject(i);
                                    Book b = new Book();
                                    b.setBookID(product.getString("bookID"));
                                    b.setLocation(product.getString("location"));
                                    b.setShelf(product.getString("shelf"));
                                    b.setBarcode(product.getString("barcode"));
                                    b.setAuthor(product.getString("author"));
                                    b.setName(product.getString("name"));
                                    b.setYear(product.getString("year"));
                                    b.setPublisher(product.getString("publisher"));
                                    b.setStatus(product.getString("status"));
                                    books.add(b);
                                }
                                query.setText("");
                                Intent intent = new Intent(SearchBook.this, SearchResultsActivity.class);
                                intent.putParcelableArrayListExtra("bookList",books);
                                startActivity(intent);

                            } else {
                                // books with search id not found
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
