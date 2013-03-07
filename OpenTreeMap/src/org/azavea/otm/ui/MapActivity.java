package org.azavea.otm.ui;


import org.azavea.otm.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Window;
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
	 
	protected void showPhotoDetail(Bitmap photo) {
		Dialog d = new Dialog(this);
		d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		d.setContentView(getLayoutInflater().inflate(R.layout.large_photo_layout, null));
		ImageView iv = (ImageView)d.findViewById(R.id.photo_detail);
		iv.setImageBitmap(photo);
		d.show();
	}
}
