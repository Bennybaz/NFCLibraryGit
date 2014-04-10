package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Lidor on 06/04/14.
 */
public class SearchResultsActivity extends Activity {

    ArrayList<Book> books;

    Context context;
    ListView lv;
    public MySimpleArrayAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results_list);
        books =  getIntent().getParcelableArrayListExtra("bookList");

        lv = (ListView) findViewById(R.id.searchResultsList);


        adapter = new MySimpleArrayAdapter(this, books);
        lv.setAdapter(adapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                Toast.makeText(context, "SHIT", Toast.LENGTH_LONG).show();
                Intent intent2 = new Intent(SearchResultsActivity.this, SearchResultActivity.class);
                intent2.putParcelableArrayListExtra("books", books);
                intent2.putExtra("position", position);
                startActivity(intent2);

            }
        });


    }
}