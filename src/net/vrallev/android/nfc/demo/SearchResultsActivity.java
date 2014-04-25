package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Lidor on 06/04/14.
 */
public class SearchResultsActivity extends Activity {

    ArrayList<Book> books; //contains the books from search query

    Context context;
    ListView lv;
    public SearchResultsAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {
        context=this;
        super.onCreate(savedInstanceState);
        setTitle("Search Results");
        setContentView(R.layout.search_results_list);
        books =  getIntent().getParcelableArrayListExtra("bookList");

        lv = (ListView) findViewById(R.id.searchResultsList);
        adapter = new SearchResultsAdapter(this, books);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent2 = new Intent(SearchResultsActivity.this, CopyListActivity.class);
                intent2.putParcelableArrayListExtra("bookList", books);
                intent2.putExtra("position", position);
                startActivity(intent2);
            }
        });
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            books.clear();
            adapter.notifyDataSetChanged();
        }
        return super.onKeyDown(keyCode, event);
    }
}