package org.azavea.otm.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;

import org.azavea.otm.R;

public class SplashScreenActivity extends Activity {
	//http://mobituts.blogspot.com/2011/10/example-of-splash-screen-in-android.html
	private static final int STOP_SPLASH = 0;     
	private static final long SPLASH_TIME_MILLIS = 750;  
  
    private Handler splashHandler = new Handler() {  
         @Override  
         public void handleMessage(Message msg) {  
              switch (msg.what) {  
                case STOP_SPLASH:  
                    Intent intent = new Intent(getApplicationContext(),   
                                            	TabLayout.class);  
                    startActivity(intent);  
                        SplashScreenActivity.this.finish();   
                    break;  
              }  
              super.handleMessage(msg);  
         }  
    };  
      
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_splash_screen);
        WebView wv = (WebView) findViewById(R.id.splash_webview);
        wv.loadUrl("file:///android_asset/splash_content.html");
        Message msg = new Message();  
        msg.what = STOP_SPLASH;  
        splashHandler.sendMessageDelayed(msg, SPLASH_TIME_MILLIS);  
    }  
}  
