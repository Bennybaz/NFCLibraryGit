package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

import net.vrallev.android.nfc.demo.ReturnRouteActivity.GetBookBarcode;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lidor on 23/04/14.
 */
public class ReturnRouteInstructions extends Activity {


    // JSON parser class
    private JSONParser jsonParser = new JSONParser();

    private static final String url_book_barcode_for_sector = "http://nfclibrary.site40.net/barcode_to_sector.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";


    ArrayList<Book> books; //contains the books details for return procedure
    int currentStep=0; //index to the current step of return
    boolean enableClick = true; //flag for disable/enable the "next" button listener

    TextView tv;
    Button nextStepBtn;
    Button returnRouteBtn;

    Context context;
    ListView lv;
    ArrayAdapter<String> adapter;

    public void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);

        setTitle("Return Route");
        setContentView(R.layout.sort_books_activity);
        books =  getIntent().getParcelableArrayListExtra("books"); //get the books from previous intent

        lv = (ListView) findViewById(R.id.sort_steps);

        //build the step array for list view
        String [] steps = new String[books.size()];
        for(int i=0;i<books.size(); i++)
            steps[i]="Step "+(i+1);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, steps);

        lv.setAdapter(adapter);

        tv = (TextView) findViewById(R.id.step_text);
        tv.setText("Press Step 1 for start return procedure");
        nextStepBtn = (Button) findViewById(R.id.next_step_button);
        nextStepBtn.setVisibility(View.GONE);

        returnRouteBtn = (Button) findViewById((R.id.return_route_button));
        returnRouteBtn.setVisibility(View.GONE);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(enableClick && currentStep==position)
                {
                    tv.setText(books.get(position).getFixedPosition()+"");
                    enableClick = false;
                    lv.getChildAt(currentStep).setBackgroundColor(Color.BLUE);
                    nextStepBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        nextStepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentStep++;
                if(currentStep==(books.size()-1))
                {
                    nextStepBtn.setVisibility(View.GONE);
                    returnRouteBtn.setVisibility(View.VISIBLE);
                }
                if(currentStep<books.size())
                {
                    lv.getChildAt(currentStep).setBackgroundColor(Color.BLUE);
                    tv.setText(books.get(currentStep).getFixedPosition()+"");
                }
            }
        });

    }


/*    class GetBookSector extends AsyncTask<String, String, String> {

        *//**
     * Getting product details in background thread
     * *//*
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

                                shelf = product.getInt("shelf");
                                sector = product.getInt("sector");
                                stand = product.getInt("stand");

                                if(!sectors.contains(sectorForAlg)) {
                                    sectors.add(sectorForAlg);
                                    barcodeSector.put(fixedPos, barcode);
                                }




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
    }*/
}