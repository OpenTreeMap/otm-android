package org.azavea.otm.ui;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.rest.RequestGenerator;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class ProfileActivity extends Activity {
	private final int PROFILE_PHOTO_USING_CAMERA = 7;
	private final int PROFILE_PHOTO_USING_GALLERY = 8;
	
	protected void changeProfilePhotoUsingCamera(Intent data) {
		Bitmap bm = (Bitmap) data.getExtras().get("data");
  		RequestGenerator rc = new RequestGenerator();
		try {
			rc.addProfilePhoto(App.getInstance(), bm, profilePhotoResponseHandler);
		} catch (JSONException e) {
			Log.e(App.LOG_TAG, "Error profile tree photo.", e);
		}
	}
	
	protected void changeProfilePhotoUsingGallery(Intent data) {
		Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(
                           selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        RequestGenerator rc = new RequestGenerator();
        Bitmap bm = BitmapFactory.decodeFile(filePath);
        try {
			rc.addProfilePhoto(App.getInstance(), bm, profilePhotoResponseHandler);
		} catch (JSONException e) {
			Log.e(App.LOG_TAG, "Error profile tree photo.", e);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Log.d(App.LOG_TAG, "Reload profile for new user login");
			if (requestCode == PROFILE_PHOTO_USING_CAMERA) {
				changeProfilePhotoUsingCamera(data);
			} else if (requestCode == PROFILE_PHOTO_USING_GALLERY) {
				changeProfilePhotoUsingGallery(data);					
			}
		} else if (resultCode == RESULT_CANCELED) {
			// Nothing?
		}
	}
	//TODO There is a lot of debugging in this function for development purposes. 
		//TODO Possible to DRY this up WRT the same handler for tree photos?
		private JsonHttpResponseHandler profilePhotoResponseHandler = new JsonHttpResponseHandler() {
			public void onSuccess(JSONObject response) {
				Log.d("AddProfilePhoto", "addTreePhotoHandler.onSuccess");
				Log.d("AddProfilePhoto", response.toString());
				try {
					if (response.get("status").equals("success")) {
						Toast.makeText(App.getInstance(), "The profile photo was added.", Toast.LENGTH_LONG).show();		
					} else {
						Toast.makeText(App.getInstance(), "Unable to add profile photo.", Toast.LENGTH_LONG).show();		
						Log.d("AddProfilePhoto", "photo response no success");
					}
				} catch (JSONException e) {
					Toast.makeText(App.getInstance(), "Unable to add profile photo", Toast.LENGTH_LONG).show();
				}
			};
			public void onFailure(Throwable e, JSONObject errorResponse) {
				Log.e("AddProfilePhoto", "addTreePhotoHandler.onFailure");
				Log.e("AddProfilePhoto", errorResponse.toString());
				Log.e("AddProfilePhoto", e.getMessage());
				Toast.makeText(App.getInstance(), "Unable to add profile photo.", Toast.LENGTH_LONG).show();		
			};
			
			protected void handleFailureMessage(Throwable e, String responseBody) {
				Log.e("addProfilePhoto", "addTreePhotoHandler.handleFailureMessage");
				Log.e("addProfilePhoto", "e.toString " + e.toString());
				Log.e("addProfilePhoto", "responseBody: " + responseBody);
				Log.e("addProfilePhoto", "e.getMessage: " + e.getMessage());
				Log.e("addProfilePhoto", "e.getCause: " + e.getCause());
				e.printStackTrace();
				Toast.makeText(App.getInstance(), "The profile photo was added.", Toast.LENGTH_LONG).show();					
			};
		};
		public void handleChangeProfilePhotoClick(View view) {
			Log.d("addProfilePhoto", "changeProfilePhoto");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setNegativeButton(R.string.use_camera, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			       			Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			       			startActivityForResult(intent, PROFILE_PHOTO_USING_CAMERA);
			           }
			       });
			builder.setPositiveButton(R.string.use_gallery, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   Intent intent = new Intent(Intent.ACTION_PICK, 
			        			   android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			       			startActivityForResult(intent, PROFILE_PHOTO_USING_GALLERY);
			           }
			       });

			AlertDialog alert = builder.create();
			alert.show();
			
		}
}
