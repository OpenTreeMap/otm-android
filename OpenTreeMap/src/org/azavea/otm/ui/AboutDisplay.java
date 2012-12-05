package org.azavea.otm.ui;

import org.azavea.otm.R;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class AboutDisplay extends Activity{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        WebView wv = (WebView) findViewById(R.id.about_webview);
        wv.loadUrl("file:///android_asset/about_content.html");
        
    }
}
