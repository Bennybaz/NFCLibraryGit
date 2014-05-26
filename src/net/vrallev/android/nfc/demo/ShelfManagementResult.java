package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Lidor on 28/04/14.
 */
public class ShelfManagementResult extends Activity {

    ArrayList<Book> notRelatedBooks;

    Context context;
    ListView lv;
    public SearchResultsActivity.SearchResultsAdapter adapter;

    Button nextShelf;
    Button finish;

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
}