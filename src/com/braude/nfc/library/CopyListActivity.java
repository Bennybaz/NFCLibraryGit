package com.braude.nfc.library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        lv = (ListView) findViewById(R.id.copyList);
        adapter = new CopyResultsAdapter(this, copy);
        lv.setAdapter(adapter);

        //new GetSearchResults().execute();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent2 = new Intent(CopyListActivity.this, SearchResultActivity.class);
        intent2.putParcelableArrayListExtra("bookList", copy);
        intent2.putExtra("position", position);
        startActivity(intent2);
            }
        });

    }

    protected void onResume() {
        super.onResume();

		/*
		 * It's important, that the activity is in the foreground (resumed). Otherwise
		 * an IllegalStateException is thrown.
		 */
        copy.clear();
        adapter.notifyDataSetChanged();
        new GetSearchResults().execute();

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
                                    b.setTagged(product.getString("isTagged"));
                                    copy.add(b);
                                    adapter.notifyDataSetChanged();
                                }

                            } else {
                                // product with pid not found
                                Toast.makeText(context, "No Such Item", Toast.LENGTH_SHORT).show();
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
     * Created by Lidor on 13/04/14.
     */
    public static class CopyResultsAdapter extends ArrayAdapter<Book>{
        private Context context;
        public ArrayList<Book> values;

        public CopyResultsAdapter(Context context, ArrayList<Book> values)
        {
            super(context, R.layout.row_layout2, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.row_layout2, parent, false);
            TextView textView1 = (TextView) rowView.findViewById(R.id.headline2);
            TextView textView2 = (TextView) rowView.findViewById(R.id.baseline2);
            textView1.setText(values.get(position).getBarcode());
            String status = new String();
            if(values.get(position).getStatus().equals("ok"))
                status = "Book exists on shelf";
            else
                status = "Book is already borrowed";
            textView2.setText(status);
            return rowView;
        }
    }
}