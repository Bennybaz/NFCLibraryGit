package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Benny on 22/03/2014.
 */
public class AboutActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.about);
        this.setRequestedOrientation(1);
    }

}
