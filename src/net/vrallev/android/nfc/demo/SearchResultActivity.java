package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
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
import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
    TextView title;
    TextView author;
    TextView publisher;
    TextView year;
    TextView shelf;
    TextView barcode;
    TextView status;

    ArrayList<Book> books; //contains the books from the search query
    int pos; //position of specific book
    Book bk; //the chosen book

    private JSONParser jsonParser = new JSONParser();
    private JSONParser jsonParser2 = new JSONParser();

    // book details in db url
    private static final String url_book_barcode_for_sector = "http://nfclibrary.site40.net/barcode_to_sector.php";
    private static final String url_book_borrow = "http://nfclibrary.site40.net/borrow_book_by_barcode.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    String FILENAME="cart";

    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result);
        setTitle("Book Info");

        if (android.os.Build.VERSION.SDK_INT > 9) {
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

        //set the book details
        title.setText(books.get(pos).getName().toString());
        author.setText(books.get(pos).getAuthor().toString());
        publisher.setText(books.get(pos).getPublisher().toString());
        year.setText(books.get(pos).getYear().toString());
        shelf.setText(books.get(pos).getShelf().toString());
        barcode.setText(books.get(pos).getBarcode().toString());

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
                new UpdateBorrow().execute();

            }
        });

        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add book to cart
                FileOutputStream fos = null;
                try {
                    fos = openFileOutput(FILENAME, Context.MODE_APPEND);
                    fos.write(barcode.getText().toString().getBytes());
                    fos.write(System.getProperty("line.separator").getBytes());
                    fos.close();
                    Toast.makeText(context,"OK",Toast.LENGTH_SHORT).show();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        if(books.get(pos).getStatus().toString().equals("ok")) {
            status.setText("Book exists on shelf");
            status.setTextColor(Color.GREEN);
        }
        else {
            status.setText("Book is already borrowed");
            status.setTextColor(Color.RED);
            getDirectionsBtn.setClickable(false);
            getDirectionsBtn.setBackgroundColor(Color.LTGRAY);
            borrowBookBtn.setClickable(false);
            borrowBookBtn.setBackgroundColor(Color.LTGRAY);
        }
    }

    class UpdateBorrow extends AsyncTask<String, String, String> {

        /* *
          * Updating book borrow in background thread
          **/
        protected String doInBackground(String... params) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;

                    try {
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("barcode", bk.getBarcode().toString()));

                        // getting student details by making HTTP request
                        // Note that product details url will use GET request
                        JSONObject json = jsonParser.makeHttpRequest(
                                url_book_borrow, "GET", params);

                        Toast.makeText(context, json.toString(), Toast.LENGTH_SHORT).show();

                        // json success tag
                        if (json != null) {
                            success = json.getInt(TAG_SUCCESS);
                            if (success == 1) {
                                Toast.makeText(context, "BORROWED", Toast.LENGTH_LONG).show();

                            } else {

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
                                if(stand==1 || stand==2) bookCase.setText("First BookCase on the left");
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
                                if(stand==23 || stand==24) bookCase.setText("Sixth BookCase on the right");


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


                                // if button is clicked, close the custom dialog
                                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        directDialog.dismiss();
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
}
