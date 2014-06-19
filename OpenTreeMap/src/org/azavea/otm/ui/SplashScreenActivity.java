package org.azavea.otm.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.webkit.WebView;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.rest.handlers.RestHandler;

public class SplashScreenActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // render the splash screen
        setContentView(R.layout.activity_splash_screen);
        WebView wv = (WebView) findViewById(R.id.splash_webview);
        wv.loadUrl("file:///android_asset/splash_content.html");

        App.getLoginManager().autoLogin(new Callback() {
            @Override
            public boolean handleMessage(final Message msg) {
                final Bundle args = msg.getData();

                // Only skip the instance switcher for a skinned app
                // or if the login succeeded on a non-skinned app
                if (args != null && args.getBoolean(RestHandler.SUCCESS_KEY)) {
                    redirect(App.hasInstanceCode());
                } else {
                    redirect(App.hasSkinCode());
                }
                return true;
            }
        });
    }

    private void redirect(final boolean skipInstanceSwitcher) {
        App app = App.getAppInstance();
        Intent intent = new Intent(app,
                skipInstanceSwitcher
                    ? TabLayout.class
                    : InstanceSwitcherActivity.class
        );

        startActivity(intent);
        finish();
    }
}
