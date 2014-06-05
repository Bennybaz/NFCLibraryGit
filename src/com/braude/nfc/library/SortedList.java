package com.braude.nfc.library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

/**
 * Created by Lidor on 13/04/14.
 */
public class SortedList extends Activity {

    ArrayList<Book> books; //contains the books details for return procedure
    ArrayList<String> sortedCommands; //contains the commands for sorting
    ArrayList<String> steps = new ArrayList<String>();
    int currentStep=0; //index to the current step of return
    boolean enableClick = true; //flag for disable/enable the "next" button listener

    TextView tv;
    Button nextStepBtn;
    Button returnRouteBtn;
    Context context;
    ListView lv;
    ArrayAdapter<String> adapter;
    int sameFlag;

    public void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);

        setTitle("Sort Books");
        setContentView(R.layout.sort_books_activity);
        books =  getIntent().getParcelableArrayListExtra("books");
        sortedCommands = getIntent().getStringArrayListExtra("srtCmd");

        sameFlag = getIntent().getIntExtra("SameFlag", 0);

        lv = (ListView) findViewById(R.id.sort_steps);

        //build the step array for list view
        for(int i=0;i<sortedCommands.size(); i++)
            steps.add("Step "+(i+1));

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
                    lv.getChildAt(currentStep).setBackgroundColor(getResources().getColor(R.color.holo_blue_dark));
                    nextStepBtn.setVisibility(View.VISIBLE);
                }
                if(sortedCommands.size()==1)
                {
                    nextStepBtn.setVisibility(View.GONE);
                    returnRouteBtn.setVisibility(View.VISIBLE);
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
                    lv.getChildAt(currentStep).setBackgroundColor(getResources().getColor(R.color.holo_blue_dark));
                    tv.setText(sortedCommands.get(currentStep));
                }
            }
        });

        returnRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SortedList.this, ReturnRouteInstructions.class);
                intent.putParcelableArrayListExtra("books",books);

                intent.putExtra("SameFlag", sameFlag);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            books.clear();
            sortedCommands.clear();
            steps.clear();
            adapter.notifyDataSetChanged();
        }
        return super.onKeyDown(keyCode, event);
    }
}