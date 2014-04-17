package org.azavea.otm.ui;

import org.azavea.otm.App;
import org.azavea.otm.data.Plot;

import android.app.Dialog;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.loopj.android.http.BinaryHttpResponseHandler;

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

    protected BinaryHttpResponseHandler getPhotoDetailHandler() {
        final MapActivity activity = this;

        return new BinaryHttpResponseHandler(Plot.IMAGE_TYPES) {
            @Override
            public void onSuccess(byte[] imageData) {
                ImageView imageView = new ImageView(activity);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));

                Dialog d = new Dialog(activity);
                d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                d.setContentView(imageView);
                d.show();
            }

            @Override
            public void onFailure(Throwable e, byte[] imageData) {
                Log.e(App.LOG_TAG, "Could not retreive tree image", e);
                Toast.makeText(getApplicationContext(), "Could not retrieve full image", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
