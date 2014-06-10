package com.braude.nfc.library;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lidor on 28/04/14.
 */
public class ShelfManagementResult extends Activity {

    ArrayList<Book> notRelatedBooks;
    Dialog directDialog;

    Context context;
    ListView lv;
    public SearchResultsActivity.SearchResultsAdapter adapter;

    Button nextShelf;
    Button finish;
    String barcode;

    private JSONParser jsonParser2 = new JSONParser();

    // book details in db url
    private static final String url_book_barcode_for_sector = "http://nfclibrary.site40.net/barcode_to_sector.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setTitle("Shelf Management Results");
        setContentView(R.layout.shelf_management_result);
        notRelatedBooks =  getIntent().getParcelableArrayListExtra("bookList");

        if(notRelatedBooks.size()==0)
        {
            Book b=new Book();
            b.setBarcode("0");
            b.setName("All books are related to scanned shelf!");
            b.setAuthor("");
            notRelatedBooks.add(b);
        }

        lv = (ListView) findViewById(R.id.managementResultList);
        adapter = new SearchResultsActivity.SearchResultsAdapter(this, notRelatedBooks);
        lv.setAdapter(adapter);

        nextShelf = (Button) findViewById(R.id.nextShelfBtn);
        finish = (Button) findViewById(R.id.finishManage);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(notRelatedBooks.size()!=0)
                {
                    barcode = notRelatedBooks.get(position).getBarcode();
                    new GetBookSector().execute();
                }
            }
        });

        nextShelf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ShelfManagementResult.this, ShelfManagementActivity.class);
                startActivity(intent);
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ShelfManagementResult.this, LibrarianMainActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            notRelatedBooks.clear();
            adapter.notifyDataSetChanged();
        }
        return super.onKeyDown(keyCode, event);
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
                                /*if(stand==1 || stand==2) bookCase.setText("First BookCase on the left");
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
                                if(stand==23 || stand==24) bookCase.setText("Sixth BookCase on the right");*/
                                if(stand==1) bookCase.setText("BookCase #2");
                                if(stand==2) bookCase.setText("BookCase #3");
                                if(stand==3) bookCase.setText("BookCase #4");
                                if(stand==4) bookCase.setText("BookCase #5");
                                if(stand==5) bookCase.setText("BookCase #6");
                                if(stand==6) bookCase.setText("BookCase #7");
                                if(stand==7) bookCase.setText("BookCase #8");
                                if(stand==8) bookCase.setText("BookCase #9");
                                if(stand==9) bookCase.setText("BookCase #10");
                                if(stand==10) bookCase.setText("BookCase #11");
                                if(stand==11) bookCase.setText("BookCase #13");
                                if(stand==12) bookCase.setText("BookCase #14");
                                if(stand==13) bookCase.setText("BookCase #33");
                                if(stand==14) bookCase.setText("BookCase #34");
                                if(stand==15) bookCase.setText("BookCase #35");
                                if(stand==16) bookCase.setText("BookCase #36");
                                if(stand==17) bookCase.setText("BookCase #37");
                                if(stand==18) bookCase.setText("BookCase #38");
                                if(stand==19) bookCase.setText("BookCase #39");
                                if(stand==20) bookCase.setText("BookCase #40");
                                if(stand==21) bookCase.setText("BookCase #41");
                                if(stand==22) bookCase.setText("BookCase #42");
                                if(stand==23) bookCase.setText("BookCase #43");
                                if(stand==24) bookCase.setText("BookCase #44");


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
                                Button dialogButtonScan = (Button) directDialog.findViewById(R.id.directionButtonScan);
                                dialogButtonScan.setVisibility(View.VISIBLE);


                                // if button is clicked, close the custom dialog
                                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        directDialog.dismiss();
                                    }
                                });

                                dialogButtonScan.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(ShelfManagementResult.this, ScanShelfActivity.class);
                                        intent.putExtra("barcode", barcode);
                                        startActivity(intent);
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