package org.azavea.otm.ui;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Dialog;
import android.content.Context;
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
}
