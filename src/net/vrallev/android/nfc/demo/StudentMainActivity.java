package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import libalg.*;

/**
 * Created by Benny on 17/03/14.
 */
public class StudentMainActivity extends Activity {

    private Button searchBtn;
    private Button borrowBtn;
    private TextView greeting;

    public void onCreate(Bundle savedInstanceState) {
        final Context context = this;
        setTitle("Reader Menu");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_main);

        searchBtn = (Button) findViewById(R.id.searchBtn);
        borrowBtn = (Button) findViewById(R.id.borrowBtn);
        greeting = (TextView) findViewById(R.id.greetText);

        final String studentID = getIntent().getExtras().getString("ID").substring(1);

        greeting.setText("Hello, "+studentID);

        searchBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, SearchBook.class);
                intent.putExtra("ID", studentID);
                startActivity(intent);
            }

        });

        borrowBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


            }
        });
    }
}
