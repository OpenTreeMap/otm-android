package org.azavea.otm.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.azavea.otm.App;
import org.azavea.otm.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.View;

public abstract class PhotoActivity extends Activity {
	public static int PHOTO_USING_CAMERA_RESPONSE = 7;
	public static int PHOTO_USING_GALLERY_RESPONSE = 8;
	
	private static String LOG_TAG = "PHOTO_ACTIVITY";
	private static String outputFilePath;
	
	/*
	 * UI Event Handlers
	 */
	
	// Bind your change photo button to this handler.
	public void handleChangePhotoClick(View view) {
		Log.d(LOG_TAG, "changePhoto");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setNegativeButton(R.string.use_camera, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	       			Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	       			try {
						File outputFile = createTempImageFile();
						outputFilePath = outputFile.getAbsolutePath();
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile));
						startActivityForResult(intent, PHOTO_USING_CAMERA_RESPONSE);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  			
	       			
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

	public static File createTempImageFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "otm_tmp_" + timeStamp + "_";

		File image = new File(
			    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), imageFileName);  
		
	    return image;
	}
	
	// This function is called at the end of the whole camera process. You might
	// want to call your rc.submit method here, or store the bm in a class level
	// variable.
	abstract void submitBitmap(Bitmap bm);

	protected void changePhotoUsingCamera(String filePath) {
		Bitmap bm = getScaledImage(filePath);
		Bitmap bmCorrect = rotateImage(getApplicationContext(), bm, Uri.parse(filePath));
		submitBitmap(bmCorrect);
	}
	
	protected void changePhotoUsingGallery(Intent data) {
		Uri selectedImage = data.getData();
        Bitmap bm = retrieveBitmapFromGallery(selectedImage);
        Bitmap bmCorrect = rotateImage(getApplicationContext(), bm, selectedImage);
        submitBitmap(bmCorrect);
	}

	// Note: You may need to override this method if your activity
	//       requires more activity results.  In that case, you
	//       should be able to call super.onActivityResult first.
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == PHOTO_USING_CAMERA_RESPONSE) {
				changePhotoUsingCamera(outputFilePath);
			} else if (requestCode == PHOTO_USING_GALLERY_RESPONSE) {
				changePhotoUsingGallery(data);					
			}
		}
	}
	
	protected Bitmap retrieveBitmapFromGallery(Uri selectedImage) {
		String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(
                           selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        
        cursor.close();
        return getScaledImage(filePath);
	}

	public static Bitmap getScaledImage(String filePath) {
		// Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/1024, photoH/768);
      
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
      
        return BitmapFactory.decodeFile(filePath, bmOptions);
	}
	
	public static Bitmap rotateImage(Context context, Bitmap sourceBitmap, Uri uri) {
		Matrix matrix = new Matrix();
		float rotation = rotationForImage(context, uri);
		if (rotation != 0f) {
		     matrix.preRotate(rotation);
		}
		Bitmap resizedBitmap = Bitmap.createBitmap(
			     sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
		
		return resizedBitmap;
	}
	
	public static float rotationForImage(Context context, Uri uri) {
		String scheme = uri.getScheme();
		if (scheme != null && scheme.equals("content")) {
	        String[] projection = { Images.ImageColumns.ORIENTATION };
	        Cursor c = context.getContentResolver().query(
	                uri, projection, null, null, null);
	        if (c.moveToFirst()) {
	            return c.getInt(0);
	        }
	    } else  {
	        try {
	            ExifInterface exif = new ExifInterface(uri.getPath());
	            int rotation = (int)exifOrientationToDegrees(
	                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
	                            ExifInterface.ORIENTATION_NORMAL));
	            return rotation;
	        } catch (IOException e) {
	            Log.e(App.LOG_TAG, "Error checking exif", e);
	            
	        }
	    }
	        return 0f;
    }

    private static float exifOrientationToDegrees(int exifOrientation) {
	    if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
	        return 90;
	    } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
	        return 180;
	    } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
	        return 270;
	    }
	    return 0;
    }
}
