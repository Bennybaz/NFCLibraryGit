package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Benny on 22/03/2014.
 */
public class SearchResultActivity extends Activity{

    final Context context = this;
    private Button getDirectionsBtn;
    private Dialog directDialog;

    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result);

        getDirectionsBtn = (Button) findViewById(R.id.writeNfcBtn);

        // add button listener
        getDirectionsBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // custom dialog
                directDialog = new Dialog(context);
                directDialog.setContentView(R.layout.direction_dialog);
                directDialog.setTitle("Directions");
                TextView bookCase = (TextView) directDialog.findViewById(R.id.textBC);
                TextView shelf = (TextView) directDialog.findViewById(R.id.textShelf);

                // fill according to bookcase location
                bookCase.setText("");
                shelf.setText("");
                ImageView image = (ImageView) directDialog.findViewById(R.id.directImage);
                //change source according to book location
                image.setImageResource(R.drawable.ic_launcher);
                Button dialogButtonCancel = (Button) directDialog.findViewById(R.id.directionButtonCancel);


                // if button is clicked, close the custom dialog
                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        directDialog.dismiss();
                    }
                });

                directDialog.show();
            }
        });


    }
}
