package org.azavea.otm.ui;

import android.app.Dialog;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public abstract class MapActivity extends android.support.v4.app.FragmentActivity {

    @Override
    protected void onResume() {
        super.onResume();
        int googlePlayStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(MapActivity.this);
        if (googlePlayStatus != ConnectionResult.SUCCESS) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(googlePlayStatus, this, 1);
            dialog.show();
        }

    }

    protected void showPhotoDetail(String photoUrl) {
        if (photoUrl == null) {
            return;
        }
        String html = "<img width=100% src='" + photoUrl + "'></img>";
        Log.d("PHOTO_HTML", html);
        WebView webview = new WebView(this);
        webview.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        Dialog d = new Dialog(this);
        d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(webview);
        d.show();
    }
}
