package net.vrallev.android.nfc.demo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


/**
 * Created by Benny on 17/03/14.
 */

public class WebActivity extends Activity {

    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.webcontent);

        class MyJavaScriptInterface
        {
            @SuppressWarnings("unused")
            public void processHTML(String html)
            {
                Toast.makeText(getApplicationContext(), html, Toast.LENGTH_LONG).show();
                // process the html as needed by the app
            }
        }

        webView = (WebView) findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        webView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.startsWith("braude.")) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }

            public void onPageFinished(WebView view, String url)
            {
        /* This call inject JavaScript into the page which just finished loading. */
                webView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }
        });

        String query = getIntent().getExtras().getString("returnkey1");
        String field = getIntent().getExtras().getString("returnkey2");

        String searchUrl ="http://braude.exlibris.co.il/F/CLXR1NLXNMR6RMEAFFDUR1YL56VD4QIPYE24MM3R8TCL83J9LH-11059?func=find-e&request="+query+"&find_scan_code=SCAN_TIT&local_base=OBC01&x=28&y=3";

        webView.loadUrl(searchUrl);

        //String customHtml = "<html><body><h2>Greetings from JavaCodeGeeks</h2></body></html>";
        //webView.loadData(customHtml, "text/html", "UTF-8");

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN) {
            switch(keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if(webView.canGoBack()){
                        webView.goBack();
                        return true;
                    }
                    break;
            }

        }
        return super.onKeyDown(keyCode, event);
    }



}