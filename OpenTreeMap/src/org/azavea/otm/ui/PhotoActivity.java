package org.azavea.otm.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.azavea.otm.App;

import android.content.Context;
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
import android.widget.Toast;

public class PhotoActivity {
	public static double PHOTO_HEIGHT = 768;
	public static double PHOTO_WIDTH = 1024;

	public static int PHOTO_USING_CAMERA_RESPONSE = 7;
	public static int PHOTO_USING_GALLERY_RESPONSE = 8;

	private static String LOG_TAG = "PHOTO_ACTIVITY";
	private static String outputFilePath;

	/*
	 * Helper functions
	 */

	public static File createImageFile() throws IOException {
		if (!isExternalStorageWritable()) {
			Toast.makeText(App.getAppInstance(),
					"Unable to write to filesystem.  If you are connected via USB, remove and try again",
					Toast.LENGTH_LONG).show();
		}

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "otm_tmp_" + timeStamp + ".jpg";

		File localImageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/TreeMap");
		if (!localImageDir.exists()) {
			if (!localImageDir.mkdirs()) {
				localImageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			}
		}
		File image = new File(localImageDir.getAbsolutePath(), imageFileName);

	    return image;
	}

	public static Bitmap getCorrectedCameraBitmap(String filePath) {
		// Add the original file to the device's gallery
		galleryAddPic(filePath);
		
		// Scale and re-orient the image, save to server
		Bitmap bm = getScaledImage(filePath);
		return rotateImage(App.getAppInstance(), bm, Uri.parse(filePath));
	}
	
	public static Bitmap getCorrectedGalleryBitmap(Intent data) {
		Uri selectedImage = data.getData();
        Bitmap bm = retrieveBitmapFromGallery(selectedImage);
        return rotateImage(App.getAppInstance(), bm, selectedImage);
	}
	
	protected static Bitmap retrieveBitmapFromGallery(Uri selectedImage) {
		String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = App.getAppInstance().getContentResolver().query(
                           selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        
        cursor.close();
        return getScaledImage(filePath);
	}

	private static void galleryAddPic(String filePath) {
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    File f = new File(filePath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    App.getAppInstance().sendBroadcast(mediaScanIntent);
	}
	
	private static Bitmap getScaledImage(String filePath) {
		// Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = (int) Math.ceil(Math.min(photoW/PHOTO_WIDTH, photoH/PHOTO_HEIGHT));
      
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
      
        return BitmapFactory.decodeFile(filePath, bmOptions);
	}
	
	private static Bitmap rotateImage(Context context, Bitmap sourceBitmap, Uri uri) {
		Matrix matrix = new Matrix();
		float rotation = rotationForImage(context, uri);
		if (rotation != 0f) {
		     matrix.preRotate(rotation);
		}
		Bitmap resizedBitmap = Bitmap.createBitmap(
			     sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
		
		return resizedBitmap;
	}

	private static float rotationForImage(Context context, Uri uri) {
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

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
