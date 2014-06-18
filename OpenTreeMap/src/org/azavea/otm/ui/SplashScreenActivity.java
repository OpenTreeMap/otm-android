package org.azavea.otm.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;

import org.azavea.otm.App;
import org.azavea.otm.R;

public class SplashScreenActivity extends Activity {
    private static final long SPLASH_TIME_MILLIS = 2000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // render the splash screen
        setContentView(R.layout.activity_splash_screen);
        WebView wv = (WebView) findViewById(R.id.splash_webview);
        wv.loadUrl("file:///android_asset/splash_content.html");

        // transition away from splash screen when time elapses
        Handler splashDestroyer = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                App app = App.getAppInstance();

                Intent intent = new Intent(app,
                        app.hasInstanceCode() ?
                                TabLayout.class :
                                InstanceSwitcherActivity.class
                );

                startActivity(intent);
                finish();
            }
        };
        splashDestroyer.sendMessageDelayed(Message.obtain(),
                SPLASH_TIME_MILLIS);
    }
}  
