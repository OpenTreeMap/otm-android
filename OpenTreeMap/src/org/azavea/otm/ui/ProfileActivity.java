package org.azavea.otm.ui;

import org.azavea.otm.App;
import org.azavea.otm.R;
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

public abstract class ProfileActivity extends Activity {
	protected static int PHOTO_USING_CAMERA_RESPONSE = 7;
	protected static int PHOTO_USING_GALLERY_RESPONSE = 8;
	protected final static int PROFILE_PIC_WIDTH = 100;
	
	/*
	 * UI Event Handlers
	 */

	// Bind your change profile photo button to this handler.
	public void handleChangeProfilePhotoClick(View view) {
		Log.d("addProfilePhoto", "changeProfilePhoto");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setNegativeButton(R.string.use_camera, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		       			Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		       			startActivityForResult(intent, PHOTO_USING_CAMERA_RESPONSE);
		           }
		       });
		builder.setPositiveButton(R.string.use_gallery, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   Intent intent = new Intent(Intent.ACTION_PICK, 
		        			   android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		       			startActivityForResult(intent, PHOTO_USING_GALLERY_RESPONSE);
		           }
		       });

		AlertDialog alert = builder.create();
		alert.show();
		
	}	

	/*
	 * Helper functions
	 */
	
	// This function is called at the end of the whole camera process. You might
	// want to call your rc.submit method here, or store the bm in a class level
	// variable.
	abstract void submitBitmap(Bitmap bm);

	protected void changeProfilePhotoUsingCamera(Intent data) {
		Bitmap bm = (Bitmap) data.getExtras().get("data");
		bm = scaleBitmap(bm, PROFILE_PIC_WIDTH);
		submitBitmap(bm);
	}
	
	protected void changeProfilePhotoUsingGallery(Intent data) {
		Uri selectedImage = data.getData();
        Bitmap bm = retrieveBitmapFromGallery(selectedImage);
        bm = scaleBitmap(bm, PROFILE_PIC_WIDTH);
        submitBitmap(bm);
	}

	
	// Note: You may need to override this method if your activity
	//       requires more activity results.  In that case, you
	//       should be able to call super.onActivityResult first.
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Log.d(App.LOG_TAG, "Reload profile for new user login");
			if (requestCode == PHOTO_USING_CAMERA_RESPONSE) {
				changeProfilePhotoUsingCamera(data);
			} else if (requestCode == PHOTO_USING_GALLERY_RESPONSE) {
				changeProfilePhotoUsingGallery(data);					
			}
		}
	}
	
	protected static Bitmap scaleBitmap(Bitmap bm, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float newHeight = (float)height/(float)width * (float)newWidth;
		return Bitmap.createScaledBitmap(bm, newWidth, (int)newHeight, false);
	}

	
	protected Bitmap retrieveBitmapFromGallery(Uri selectedImage) {
		String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(
                           selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        
        cursor.close();
        Bitmap fullSizeBitmap = BitmapFactory.decodeFile(filePath);
        return fullSizeBitmap;
	}

	
}
