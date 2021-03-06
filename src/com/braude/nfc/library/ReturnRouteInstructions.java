package com.braude.nfc.library;

import android.app.Activity;
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
 * Created by Lidor on 23/04/14.
 */
public class ReturnRouteInstructions extends Activity {


    // JSON parser class
    private JSONParser jsonParser = new JSONParser();
    private JSONParser jsonParser3 = new JSONParser();

    private static final String url_book_barcode_for_sector = "http://nfclibrary.site40.net/barcode_to_sector.php";
    private static final String url_return_book_by_barcode = "http://nfclibrary.site40.net/return_book_by_barcode.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";


    ArrayList<Book> books; //contains the books details for return procedure
    int currentStep=0; //index to the current step of return
    boolean enableClick = true; //flag for disable/enable the "next" button listener
    int [][] pos;

    TextView tv;
    Button nextStepBtn;
    Button returnRouteBtn;

    Context context;
    ListView lv;
    ArrayAdapter<String> adapter;
    ArrayList<String> steps = new ArrayList<String>();
    int sameFlag;

    public void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);

        setTitle("Return Route");
        setContentView(R.layout.return_instructions);
        books =  getIntent().getParcelableArrayListExtra("books"); //get the books from previous intent
        pos=new int[books.size()][3];


        sameFlag = getIntent().getIntExtra("SameFlag", 0);

        new GetBookSector().execute();

        lv = (ListView) findViewById(R.id.return_sort_steps);

        //build the step array for list view
        if(sameFlag==1)
        {
            for(int i=0;i<books.size(); i++)
                steps.add("Step "+(i+1));
        }
        else if(sameFlag==0)
            steps.add("Step 1");

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, steps);

        lv.setAdapter(adapter);

        tv = (TextView) findViewById(R.id.return_step_text);
        //tv.setText("Press Step 1 for start return procedure");
        nextStepBtn = (Button) findViewById(R.id.return_next_step_button);
        nextStepBtn.setVisibility(View.GONE);

        returnRouteBtn = (Button) findViewById((R.id.return_return_route_button));
        returnRouteBtn.setVisibility(View.GONE);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (enableClick && currentStep == position) {

                    TextView bookCase = (TextView) findViewById(R.id.return_textBC);
                    TextView shelff = (TextView) findViewById(R.id.return_textShelf);

                    // fill according to bookcase location
                    /*if (pos[position][2] == 1 || pos[position][2] == 2) bookCase.setText("First BookCase on the left");
                    if (pos[position][2] == 3 || pos[position][2] == 4) bookCase.setText("Second BookCase on the left");
                    if (pos[position][2] == 5 || pos[position][2] == 6) bookCase.setText("Third BookCase on the left");
                    if (pos[position][2] == 7 || pos[position][2] == 8) bookCase.setText("Fourth BookCase on the left");
                    if (pos[position][2] == 9 || pos[position][2] == 10) bookCase.setText("Fifth BookCase on the left");
                    if (pos[position][2] == 11 || pos[position][2] == 12)
                        bookCase.setText("Sixth BookCase on the left");
                    if (pos[position][2] == 13 || pos[position][2] == 14)
                        bookCase.setText("First BookCase on the right");
                    if (pos[position][2] == 15 || pos[position][2] == 16)
                        bookCase.setText("Second BookCase on the right");
                    if (pos[position][2] == 17 || pos[position][2] == 18)
                        bookCase.setText("Third BookCase on the right");
                    if (pos[position][2] == 19 || pos[position][2] == 20)
                        bookCase.setText("Fourth BookCase on the right");
                    if (pos[position][2] == 21 || pos[position][2] == 22)
                        bookCase.setText("Fifth BookCase on the right");
                    if (pos[position][2] == 23 || pos[position][2] == 24)
                        bookCase.setText("Sixth BookCase on the right");*/

                    if(pos[position][2]==1) bookCase.setText("BookCase #2");
                    if(pos[position][2]==2) bookCase.setText("BookCase #3");
                    if(pos[position][2]==3) bookCase.setText("BookCase #4");
                    if(pos[position][2]==4) bookCase.setText("BookCase #5");
                    if(pos[position][2]==5) bookCase.setText("BookCase #6");
                    if(pos[position][2]==6) bookCase.setText("BookCase #7");
                    if(pos[position][2]==7) bookCase.setText("BookCase #8");
                    if(pos[position][2]==8) bookCase.setText("BookCase #9");
                    if(pos[position][2]==9) bookCase.setText("BookCase #10");
                    if(pos[position][2]==10) bookCase.setText("BookCase #11");
                    if(pos[position][2]==11) bookCase.setText("BookCase #13");
                    if(pos[position][2]==12) bookCase.setText("BookCase #14");
                    if(pos[position][2]==13) bookCase.setText("BookCase #33");
                    if(pos[position][2]==14) bookCase.setText("BookCase #34");
                    if(pos[position][2]==15) bookCase.setText("BookCase #35");
                    if(pos[position][2]==16) bookCase.setText("BookCase #36");
                    if(pos[position][2]==17) bookCase.setText("BookCase #37");
                    if(pos[position][2]==18) bookCase.setText("BookCase #38");
                    if(pos[position][2]==19) bookCase.setText("BookCase #39");
                    if(pos[position][2]==20) bookCase.setText("BookCase #40");
                    if(pos[position][2]==21) bookCase.setText("BookCase #41");
                    if(pos[position][2]==22) bookCase.setText("BookCase #42");
                    if(pos[position][2]==23) bookCase.setText("BookCase #43");
                    if(pos[position][2]==24) bookCase.setText("BookCase #44");


                    if (pos[position][0] == 1) shelff.setText("Top Shelf");
                    if (pos[position][0] == 2) shelff.setText("2nd Shelf from top");
                    if (pos[position][0] == 3) shelff.setText("3rd Shelf from top");
                    if (pos[position][0] == 4) shelff.setText("4th Shelf from top");
                    if (pos[position][0] == 5) shelff.setText("Bottom Shelf");


                    TextView sectorr = (TextView) findViewById(R.id.return_sector_pos);
                    //ImageView image = (ImageView) findViewById(R.id.return_directImage);
                    //change source according to book location
                    /*if (pos[position][1] % 2 == 0) {
                        if (pos[position][1] == 1) image.setImageResource(R.drawable.sector_five);
                        if (pos[position][1] == 2) image.setImageResource(R.drawable.sector_six);
                        if (pos[position][1] == 3) image.setImageResource(R.drawable.sector_seven);
                        if (pos[position][1] == 4) image.setImageResource(R.drawable.sector_eight);

                    } else {
                        if (pos[position][1] == 1) image.setImageResource(R.drawable.sector_one);
                        if (pos[position][1] == 2) image.setImageResource(R.drawable.sector_two);
                        if (pos[position][1] == 3) image.setImageResource(R.drawable.sector_three);
                        if (pos[position][1] == 4) image.setImageResource(R.drawable.sector_four);
                    }*/

                    if(pos[position][1]==1) sectorr.setText("First sector from the beginning");
                    if(pos[position][1]==2) sectorr.setText("Second sector from the beginning");
                    if(pos[position][1]==3) sectorr.setText("Third sector from the beginning");
                    if(pos[position][1]==4) sectorr.setText("Fourth sector from the beginning");

                    if(sameFlag==1)
                        tv.setText("Place First Book Here:");
                    if(sameFlag==0)
                        tv.setText("Place All the Book Here:");
                    enableClick = false;
                    lv.getChildAt(currentStep).setBackgroundColor(getResources().getColor(R.color.holo_blue_dark));
                    nextStepBtn.setVisibility(View.VISIBLE);
                }

                if(books.size()==1 || sameFlag==0)
                {
                    nextStepBtn.setVisibility(View.GONE);
                    returnRouteBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        returnRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //update the book return on DB
                new UpdateBookStatus().execute();

                Intent intent = new Intent(ReturnRouteInstructions.this, LibrarianMainActivity.class);
                startActivity(intent);
            }
        });

        nextStepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentStep++;
                if (currentStep == (books.size() - 1)) {
                    nextStepBtn.setVisibility(View.GONE);
                    returnRouteBtn.setVisibility(View.VISIBLE);
                }
                if (currentStep < books.size()) {
                    lv.getChildAt(currentStep).setBackgroundColor(getResources().getColor(R.color.holo_blue_dark));
                    TextView bookCase = (TextView) findViewById(R.id.return_textBC);
                    TextView shelff = (TextView) findViewById(R.id.return_textShelf);

                    if (pos[currentStep][2] == 1) bookCase.setText("BookCase #2");
                    if (pos[currentStep][2] == 2) bookCase.setText("BookCase #3");
                    if (pos[currentStep][2] == 3) bookCase.setText("BookCase #4");
                    if (pos[currentStep][2] == 4) bookCase.setText("BookCase #5");
                    if (pos[currentStep][2] == 5) bookCase.setText("BookCase #6");
                    if (pos[currentStep][2] == 6) bookCase.setText("BookCase #7");
                    if (pos[currentStep][2] == 7) bookCase.setText("BookCase #8");
                    if (pos[currentStep][2] == 8) bookCase.setText("BookCase #9");
                    if (pos[currentStep][2] == 9) bookCase.setText("BookCase #10");
                    if (pos[currentStep][2] == 10) bookCase.setText("BookCase #11");
                    if (pos[currentStep][2] == 11) bookCase.setText("BookCase #13");
                    if (pos[currentStep][2] == 12) bookCase.setText("BookCase #14");
                    if (pos[currentStep][2] == 13) bookCase.setText("BookCase #33");
                    if (pos[currentStep][2] == 14) bookCase.setText("BookCase #34");
                    if (pos[currentStep][2] == 15) bookCase.setText("BookCase #35");
                    if (pos[currentStep][2] == 16) bookCase.setText("BookCase #36");
                    if (pos[currentStep][2] == 17) bookCase.setText("BookCase #37");
                    if (pos[currentStep][2] == 18) bookCase.setText("BookCase #38");
                    if (pos[currentStep][2] == 19) bookCase.setText("BookCase #39");
                    if (pos[currentStep][2] == 20) bookCase.setText("BookCase #40");
                    if (pos[currentStep][2] == 21) bookCase.setText("BookCase #41");
                    if (pos[currentStep][2] == 22) bookCase.setText("BookCase #42");
                    if (pos[currentStep][2] == 23) bookCase.setText("BookCase #43");
                    if (pos[currentStep][2] == 24) bookCase.setText("BookCase #44");


                    if (pos[currentStep][0] == 1) shelff.setText("Top Shelf");
                    if (pos[currentStep][0] == 2) shelff.setText("2nd Shelf from top");
                    if (pos[currentStep][0] == 3) shelff.setText("3rd Shelf from top");
                    if (pos[currentStep][0] == 4) shelff.setText("4th Shelf from top");
                    if (pos[currentStep][0] == 5) shelff.setText("Bottom Shelf");

                    TextView sectorr = (TextView) findViewById(R.id.return_sector_pos);
                    //change source according to book location

                    if (pos[currentStep][1] == 1) sectorr.setText("First sector from the beginning");
                    if (pos[currentStep][1] == 2) sectorr.setText("Second sector from the beginning");
                    if (pos[currentStep][1] == 3) sectorr.setText("Third sector from the beginning");
                    if (pos[currentStep][1] == 4) sectorr.setText("Fourth sector from the beginning");


                    if (sameFlag == 1)
                        tv.setText("Place First Book Here:");
                    if (sameFlag == 0)
                        tv.setText("Place All the Book Here:");
                }
            }
        });

    }


    class GetBookSector extends AsyncTask<String, String, String> {

     //
     // Getting product details in background thread
     //
        protected String doInBackground(String... params) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;
                    for(int i=0;i<books.size();i++)
                    {
	                    try {
	                        // Building Parameters
	                        List<NameValuePair> params = new ArrayList<NameValuePair>();
	                        params.add(new BasicNameValuePair("barcode", books.get(i).getBarcode()));
	
	                        // getting student details by making HTTP request
	                        // Note that product details url will use GET request
	
	                        JSONObject json = jsonParser.makeHttpRequest(
	                                url_book_barcode_for_sector, "GET", params);
	
	                        // json success tag
	                        if(json!=null) {
	                            success = json.getInt(TAG_SUCCESS);
	                            if (success == 1) {
	
	                                // successfully received product details
	                                JSONArray productObj = json.getJSONArray("reader"); // JSON Array
	
	                                // get first user object from JSON Array
	                                JSONObject product = productObj.getJSONObject(0);
	
	                                pos[i][0] = product.getInt("shelf");
	                                pos[i][1] = product.getInt("sector");
	                                pos[i][2] = product.getInt("stand");
		
	
	                            } else {
                                    Toast.makeText(context,"Error: cannot find the book location",Toast.LENGTH_SHORT).show();
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
                    ArrayList<Book> tempBooks = new ArrayList<Book>(books);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            books.clear();
            steps.clear();
            adapter.notifyDataSetChanged();
        }
        return super.onKeyDown(keyCode, event);
    }
}