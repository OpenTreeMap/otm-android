package org.azavea.otm.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.webkit.WebView;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.rest.handlers.RestHandler;
import org.jdeferred.Deferred;
import org.jdeferred.DeferredFutureTask;
import org.jdeferred.DeferredManager;
import org.jdeferred.DeferredRunnable;
import org.jdeferred.DoneCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.AndroidDeferredObject;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.impl.DeferredPromise;

import java.util.concurrent.Callable;

public class SplashScreenActivity extends Activity {

    private static final int SPLASH_SCREEN_DELAY_MILLIS = 2000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // render the splash screen
        setContentView(R.layout.activity_splash_screen);
        WebView wv = (WebView) findViewById(R.id.splash_webview);
        wv.loadUrl("file:///android_asset/splash_content.html");

        // The 2nd and 3rd type parameters are required but unused
        // (They would be the arguments for promise.reject and promise.progress)
        final Deferred<Bundle, Throwable, Integer> autoLoginDeferred =
                new DeferredObject<>();

        final Promise<Bundle, Throwable, Integer> autoLogin = autoLoginDeferred.promise();

        App.getLoginManager().autoLogin(msg -> {
            autoLoginDeferred.resolve(msg.getData());
            return true;
        });

        Handler splashHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                autoLogin.done(args -> {
                    // Only skip the instance switcher for a skinned app
                    // or if the login succeeded on a non-skinned app
                    if (args != null && args.getBoolean(RestHandler.SUCCESS_KEY)) {
                        redirect(App.hasInstanceCode());
                    } else {
                        redirect(App.hasSkinCode());
                    }
                });
            }
        };
        splashHandler.sendMessageDelayed(Message.obtain(), SPLASH_SCREEN_DELAY_MILLIS);
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
