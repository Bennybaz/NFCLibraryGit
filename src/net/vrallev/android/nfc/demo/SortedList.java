package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

/**
 * Created by Lidor on 13/04/14.
 */
public class SortedList extends Activity {

    ArrayList<Book> books; //contains the books details for return procedure 
    ArrayList<String> sortedCommands; //contains the commands for sorting 
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

        setTitle("Sort Books");
        setContentView(R.layout.sort_books_activity);
        books =  getIntent().getParcelableArrayListExtra("books");
        sortedCommands = getIntent().getStringArrayListExtra("srtCmd");

        lv = (ListView) findViewById(R.id.sort_steps);

        //build the step array for list view
        String [] steps = new String[sortedCommands.size()];
        for(int i=0;i<sortedCommands.size(); i++)
            steps[i]="Step "+(i+1);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, steps);

        lv.setAdapter(adapter);

        tv = (TextView) findViewById(R.id.step_text);
        nextStepBtn = (Button) findViewById(R.id.next_step_button);
        nextStepBtn.setVisibility(View.GONE);

        returnRouteBtn = (Button) findViewById((R.id.return_route_button));
        returnRouteBtn.setVisibility(View.GONE);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(enableClick && currentStep==position)
                {
                    tv.setText(sortedCommands.get(position));
                    enableClick = false;
                    //lv.getChildAt(currentStep).setBackgroundColor(R.color.holo_purple);
                    lv.getChildAt(currentStep).setBackgroundColor(Color.BLUE);
                    nextStepBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        nextStepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentStep++;
                if(currentStep==(sortedCommands.size()-1))
                {
                    nextStepBtn.setVisibility(View.GONE);
                    returnRouteBtn.setVisibility(View.VISIBLE);
                }
                if(currentStep<sortedCommands.size())
                {
                    //lv.getChildAt(currentStep).setBackgroundColor(R.color.holo_purple);
                    lv.getChildAt(currentStep).setBackgroundColor(Color.BLUE);
                    tv.setText(sortedCommands.get(currentStep));
                }
            }
        });

        returnRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SortedList.this, ReturnRouteInstructions.class);
                intent.putParcelableArrayListExtra("books",books);
                startActivity(intent);
            }
        });

    }
}