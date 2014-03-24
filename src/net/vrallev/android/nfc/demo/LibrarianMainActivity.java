package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Benny on 21/03/2014.
 */
public class LibrarianMainActivity extends Activity {

    private Button searchBtn;
    private Button returnBtn;
    private TextView greeting;

    public void onCreate(Bundle savedInstanceState) {
        final Context context = this;
        setTitle("Librarian Menu");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.librarian_main);

        searchBtn = (Button) findViewById(R.id.searchBtn);
        returnBtn = (Button) findViewById(R.id.bookReturnBtn);
        greeting = (TextView) findViewById(R.id.greetText2);

        final String librarianID = getIntent().getExtras().getString("ID").substring(1);

        greeting.setText("Hello, "+librarianID);

        searchBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, SearchBook.class);
                intent.putExtra("ID", librarianID);
                startActivity(intent);
            }

        });

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LibrarianMainActivity.this, ReturnRouteActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }


}
