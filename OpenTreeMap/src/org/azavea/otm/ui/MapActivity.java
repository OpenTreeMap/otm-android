package org.azavea.otm.ui;


import org.azavea.otm.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

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
		webview.loadData(html, "text/html", null);
		Dialog d = new Dialog(this);
		d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		d.setContentView(webview);
		d.show();
	}
}
