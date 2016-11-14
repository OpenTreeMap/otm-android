package org.azavea.otm.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.joelapenna.foursquared.widget.SegmentedButton;
import com.loopj.android.http.BinaryHttpResponseHandler;

import org.azavea.helpers.Logger;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.json.JSONException;

import cz.msebera.android.httpclient.Header;

public class MapHelper {

    public static void setUpBasemapControls(SegmentedButton buttons, GoogleMap mMap) {
        final String[] mapLabels = {"map", "satellite", "hybrid"};
        final int[] mapTypes = {GoogleMap.MAP_TYPE_NORMAL,
                GoogleMap.MAP_TYPE_SATELLITE,
                GoogleMap.MAP_TYPE_HYBRID};

        // match the clicked index of mapLabels to the element of mapTypes
        buttons.clearButtons();
        buttons.addButtons(mapLabels);
        buttons.setOnClickListener((int index) -> mMap.setMapType(mapTypes[index]));
    }

    public static void checkGooglePlay(Activity activity) {
        int googlePlayStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (googlePlayStatus != ConnectionResult.SUCCESS) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(googlePlayStatus, activity, 1);
            dialog.show();
        }

    }

    protected static BinaryHttpResponseHandler getPhotoDetailHandler(final Activity activity, final Plot plot) {
        return new BinaryHttpResponseHandler(Plot.IMAGE_TYPES) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] imageData) {
                ImageView imageView = new ImageView(activity);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));

                new AlertDialog.Builder(activity)
                    .setNeutralButton(R.string.photo_report_close, ((dialog, which) -> {
                        dialog.dismiss();
                    }))
                    .setNegativeButton(R.string.photo_report_action, ((dialog, which) -> {
                        String body = "";
                        try {
                            body = String.format(activity.getString(R.string.photo_report_body),
                                    App.getCurrentInstance().getInstanceId(), plot.getId(),
                                    plot.getMostRecentPhoto().optInt("id"));
                        } catch (JSONException e) {
                            Logger.error("Could not get plot id in photo report", e);
                        }

                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {activity.getString(R.string.photo_report_email)});
                        intent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.report_photo_subject));
                        intent.putExtra(Intent.EXTRA_TEXT, body);

                        if (intent.resolveActivity(activity.getPackageManager()) != null) {
                            activity.startActivity(intent);
                        } else {
                            Toast.makeText(activity, R.string.photo_report_failure, Toast.LENGTH_LONG).show();
                        }
                    }))
                    .setView(imageView)
                    .show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] imageData, Throwable e) {
                Logger.error("Could not retreive tree image", e);
                Toast.makeText(activity.getApplicationContext(), "Could not retrieve full image", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
