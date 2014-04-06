package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Lidor on 06/04/14.
 */
public class SearchResultsActivity extends Activity {

    ArrayList<Book> books = (ArrayList<Book>) getIntent().getSerializableExtra("bookList");
    Context context;
    ListView lv;
    public MySimpleArrayAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lv = (ListView) findViewById(R.layout.search_results_list);


        adapter = new MySimpleArrayAdapter(this, books);
        lv.setAdapter(adapter);
       /* lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchResultsActivity.this, SearchResultActivity.class);
                startActivity(intent);
            }
        });*/

    }
}