package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


/**
 * Created by Benny on 17/03/14.
 */
public class SearchBook extends Activity {

    private Button button;
    private EditText query;
    private Spinner field;

    public void onCreate(Bundle savedInstanceState) {
        setTitle("Search Book");
        final Context context = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_search_main);

        button = (Button) findViewById(R.id.buttonUrl);
        query = (EditText) findViewById(R.id.queryET);
        field = (Spinner) findViewById(R.id.fieldSpinner);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String query_string = query.getText().toString();
                String field_string = field.getSelectedItem().toString();
                Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra("returnkey1",query_string);
                intent.putExtra("returnkey2",field_string);
                startActivity(intent);
            }

        });
    }
}
