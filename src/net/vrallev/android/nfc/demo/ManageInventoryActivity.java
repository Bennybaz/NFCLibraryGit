package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;

/**
 * Created by Benny on 06/04/2014.
 */
public class ManageInventoryActivity extends Activity {

    Button addNewBook;
    Button assignToShelf;
    Button manageShelf;
    private Context context;

    public void onCreate(Bundle savedInstanceState) {
        context = this;
        setTitle("Manage Inventory");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_inventory);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        addNewBook = (Button) findViewById(R.id.addNewBookBtn);
        assignToShelf = (Button) findViewById(R.id.assignToShelfBtn);
        manageShelf = (Button) findViewById((R.id.manageShelfBtn));

        addNewBook.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, AddNewBook.class);
                startActivity(intent);
            }

        });

        assignToShelf.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, AssignBookToShelfActivity.class);
                startActivity(intent);
            }

        });

        manageShelf.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, ShelfManagementActivity.class);
                startActivity(intent);
            }

        });
    }
}
