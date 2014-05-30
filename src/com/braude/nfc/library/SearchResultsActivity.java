package com.braude.nfc.library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

    /**
     * Created by Benny on 10/04/2014.
     */

    public static class SearchResultsAdapter extends ArrayAdapter<Book> {
        private Context context;
        public ArrayList<Book> values;

        public SearchResultsAdapter(Context context, ArrayList<Book> values)
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
            textView1.setText(values.get(position).getName());
            textView2.setText(values.get(position).getAuthor());
            return rowView;
        }

    }
}