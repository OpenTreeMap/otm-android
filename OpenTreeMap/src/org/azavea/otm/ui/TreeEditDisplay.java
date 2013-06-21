package org.azavea.otm.ui;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map.Entry;

import org.azavea.otm.App;
import org.azavea.otm.Field;
import org.azavea.otm.FieldGroup;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.Species;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.RestHandler;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler.Callback;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.graphics.Bitmap;

public class TreeEditDisplay extends TreeDisplay {

	protected static final int SPECIES_SELECTOR = 0;
	protected static final int TREE_PHOTO = 1;
	protected static final int TREE_MOVE = 2;
	
	private Field speciesField;
	private boolean photoHasBeenChanged;
	private ProgressDialog deleteDialog = null;
	private ProgressDialog saveDialog = null;
	private ProgressDialog savePhotoDialog = null;
		
	protected static final int PHOTO_USING_CAMERA_RESPONSE = 7;
	protected static final int PHOTO_USING_GALLERY_RESPONSE = 8;

	private Uri mImageCaptureUri;
	
	private RestHandler<Plot> deleteTreeHandler = new RestHandler<Plot>(new Plot()) {

		@Override
		public void onFailure(Throwable e, String message){
			deleteDialog.dismiss();
			Toast.makeText(App.getInstance(), "Unable to delete tree", Toast.LENGTH_SHORT).show();
			Log.e(App.LOG_TAG, "Unable to delete tree.");
		}				

		@Override
		protected void handleFailureMessage(Throwable e, String responseBody) {
			deleteDialog.dismiss();
			Toast.makeText(App.getInstance(), "Failure: Unable to delete tree", Toast.LENGTH_SHORT).show();
			Log.e(App.LOG_TAG, "Unable to delete tree.", e);
		}
		
		@Override
		public void dataReceived(Plot response) {
			deleteDialog.dismiss();
			Toast.makeText(App.getInstance(), "The tree was deleted.", Toast.LENGTH_SHORT).show();
			Intent resultIntent = new Intent();
			
			// The tree was deleted, so return to the info page, and bring along
			// the data for the new plot, which was the response from the 
			// delete operation
			resultIntent.putExtra("plot", response.getData().toString());
			setResult(RESULT_OK, resultIntent);
			finish();
		}
		
	};
	
	private JsonHttpResponseHandler deletePlotHandler = new JsonHttpResponseHandler() {
		public void onSuccess(JSONObject response) {
			try {
				if (response.getBoolean("ok")) {
					deleteDialog.dismiss();
					Toast.makeText(App.getInstance(), "The planting site was deleted.", Toast.LENGTH_SHORT).show();
					setResult(RESULT_PLOT_DELETED);
					finish();
					
				}
			} catch (JSONException e) {
				deleteDialog.dismiss();
				Toast.makeText(App.getInstance(), "Unable to delete plot", Toast.LENGTH_SHORT).show();
			}
		};
	};
	
	private JsonHttpResponseHandler addTreePhotoHandler = new JsonHttpResponseHandler() {
		public void onSuccess(JSONObject response) {
			Log.d("AddTreePhoto", "addTreePhotoHandler.onSuccess");
			Log.d("AddTreePhoto", response.toString());
			try {
				if (response.get("status").equals("success")) {
					Toast.makeText(App.getInstance(), "The tree photo was added.", Toast.LENGTH_LONG).show();	
					plot.assignNewTreePhoto(response.getString("title"), response.getInt("id"));
					photoHasBeenChanged = true;
					savePhotoDialog.dismiss();
					
				} else {
					Toast.makeText(App.getInstance(), "Unable to add tree photo.", Toast.LENGTH_LONG).show();		
					Log.d("AddTreePhoto", "photo response no success");
				}
			} catch (JSONException e) {
				Toast.makeText(App.getInstance(), "Unable to add tree photo", Toast.LENGTH_LONG).show();
			}
		};
		
		public void onFailure(Throwable e, JSONObject errorResponse) {
			Log.e("AddTreePhoto", "addTreePhotoHandler.onFailure");
			Log.e("AddTreePhoto", errorResponse.toString());
			Log.e("AddTreePhoto", e.getMessage());
			Toast.makeText(App.getInstance(), "Unable to add tree photo.", Toast.LENGTH_LONG).show();
			savePhotoDialog.dismiss();
		};
		
		protected void handleFailureMessage(Throwable e, String responseBody) {
			Log.e("addTreePhoto", "addTreePhotoHandler.handleFailureMessage");
			Log.e("addTreePhoto", "e.toString " + e.toString());
			Log.e("addTreePhoto", "responseBody: " + responseBody);
			Log.e("addTreePhoto", "e.getMessage: " + e.getMessage());
			Log.e("addTreePhoto", "e.getCause: " + e.getCause());
			e.printStackTrace();
			Toast.makeText(App.getInstance(), "The tree photo could not be added.", Toast.LENGTH_LONG).show();
			savePhotoDialog.dismiss();
		};
	};
	
	public void onCreate(Bundle savedInstanceState) {
    	mapFragmentId = R.id.vignette_map_edit_mode;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plot_edit_activity);
		setUpMapIfNeeded();
		initializeEditPage();
		photoHasBeenChanged = false;
		mMap.setOnMapClickListener( new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng point) {
				Intent treeMoveIntent = new Intent(TreeEditDisplay.this, TreeMove.class);
				treeMoveIntent.putExtra("plot", plot.getData().toString());
				startActivityForResult(treeMoveIntent, TREE_MOVE);
			}
		});
	}
	
	public void onResume() {
		super.onResume();
		plotLocation = getPlotLocation(plot);
		showPositionOnMap();
	}

	private void initializeEditPage() {
		
		if (plot == null) {
			finish();
		}

		LinearLayout fieldList = (LinearLayout) findViewById(R.id.field_list);
		LayoutInflater layout = ((Activity) this).getLayoutInflater();

		View first = null;
		// Add all the fields to the display for edit mode
		for (FieldGroup group : App.getFieldManager().getFieldGroups()) {
			View fieldGroup = group.renderForEdit(layout, plot, App.getLoginManager().loggedInUser, TreeEditDisplay.this);
			if (first == null) {
				first = fieldGroup;
			}
			if (fieldGroup != null) {
				fieldList.addView(fieldGroup);
			}
		}
		first.requestFocus();
		
		setupCircumferenceField(layout);
		setupSpeciesSelector();
		setupChangePhotoButton(layout, fieldList);
		setupDeleteButtons(layout, fieldList);		
	}
	
	private void setupCircumferenceField(LayoutInflater layout) {
		final EditText dbh = (EditText)findViewById(R.id.dynamic_dbh).findViewById(R.id.field_value);
		final EditText cir = (EditText)findViewById(R.id.dynamic_circumference).findViewById(R.id.field_value);
		
		dbh.addTextChangedListener(new TextWatcher() {
			   @Override
			    public void onTextChanged(CharSequence s, int start, int before,
			    		int count) {}

				@Override
				public void afterTextChanged(Editable s) {
					handleTextChange(dbh, cir, false);
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {}
		});

		
		cir.addTextChangedListener(new TextWatcher() {
			   @Override
			    public void onTextChanged(CharSequence s, int start, int before,
			    		int count) {}

				@Override
				public void afterTextChanged(Editable s) {
					handleTextChange(cir, dbh, true);
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {}
		});
		
		dbh.setText(dbh.getText());
	}
	
	private void handleTextChange(EditText editing, EditText receiving, boolean calcDbh) {
		try {
			DecimalFormat df = new DecimalFormat("#.##");
			
			String editingText = editing.getText().toString();
    		String other = receiving.getText().toString();
    		double otherVal = (other.equals("") || other.equals(".")) 
    				? 0 
    				: Double.parseDouble(other);
    		
    		// If the value was blanked, and the other is not already blank, blank it
			if (editingText.equals("")) {
				if (other.equals("")) {
					return;
				} else {
					receiving.setText("");
				}
				return;
			}
			
			String display = "";
			double calculatedVal = 0;
			
			// Handle cases where the first input is a decimal point
			if (editingText.equals(".")) editingText = "0.";
			
			if (calcDbh) {
				double c = Double.parseDouble(editingText);
	    		calculatedVal = c / Math.PI;
			} else {
				double d = Double.parseDouble(editingText);
        		calculatedVal = Math.PI * d;

			}
			display = df.format(calculatedVal);
    		
			// Only set the other text if there is a significant difference
			// in from the calculated value
    		if (Math.abs(otherVal - calculatedVal) >= 0.05) {
    			receiving.setText(display);
    			receiving.setSelection(display.length());
    		}
    		
    	} catch (Exception e) {
    		editing.setText("");
    	}
	}
	
	/**
	 * Delete options for tree and plot are available under certain situations
	 * as reported from the /plot API endpoint as attributes of a plot/user
	 * combo.  Don't give delete tree option if a tree isn't present
	 */
	private void setupDeleteButtons(LayoutInflater layout, LinearLayout fieldList) {
		View actionPanel = layout.inflate(R.layout.plot_edit_delete_buttons, null);
		int plotVis = View.GONE;
		int treeVis = View.GONE;
		
		try {
			if (plot.canDeletePlot()) {
				plotVis = View.VISIBLE;
			}
			if (plot.canDeleteTree() && plot.getTree() != null) {
				treeVis = View.VISIBLE;
			}
		} catch (JSONException e) {
			Log.e(App.LOG_TAG, "Cannot access plot permissions", e);
		}
		
		actionPanel.findViewById(R.id.delete_plot).setVisibility(plotVis);
		actionPanel.findViewById(R.id.delete_tree).setVisibility(treeVis);
		fieldList.addView(actionPanel);
		
	}
	
	private void setupChangePhotoButton(LayoutInflater layout, LinearLayout fieldList) {
		try {
			// You can only change a tree picture if there is a tree
			if ((plot.getId() != 0) &&   (plot.getTree() != null)) {
				View thePanel = layout.inflate(R.layout.plot_edit_photo_button, null);
				fieldList.addView(thePanel);
			}	
		} catch(Exception e) {
			Log.e(App.LOG_TAG,"Error getting tree, not allowing photo to be taken", e);
		}
	}
	
	public void confirmDelete(int messageResource, final Callback callback) {
		final Activity thisActivity = this;
		
		new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.confirm_delete)
        .setMessage(messageResource)
        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	deleteDialog = ProgressDialog.show(thisActivity, "", "Deleting...", true);
				Message resultMessage = new Message();
		    	Bundle data = new Bundle();
		    	data.putBoolean("confirm", true);
		    	resultMessage.setData(data);
		    	callback.handleMessage(resultMessage);
            }

        })
        .setNegativeButton(R.string.cancel, null)
        .show();
		
	}

	
	public void deleteTree(View view) {
		Callback confirm = new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				if (msg.getData().getBoolean("confirm")) {
					
					RequestGenerator rc = new RequestGenerator();
					try {
						rc.deleteCurrentTreeOnPlot(App.getInstance(), plot.getId(), deleteTreeHandler);
					} catch (JSONException e) {
						Log.e(App.LOG_TAG, "Error deleting tree", e);
					}
				}
				return true;
			}
		};
		
		confirmDelete(R.string.confirm_delete_tree_msg, confirm);
	}
	
	public void deletePlot(View view) {
		Callback confirm = new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				if (msg.getData().getBoolean("confirm")) {
					RequestGenerator rc = new RequestGenerator();
					try {
						rc.deletePlot(App.getInstance(), plot.getId(), deletePlotHandler);
					} catch (JSONException e) {
						Log.e(App.LOG_TAG, "Error deleting tree plot", e);
					}
				}
				return true;
			}
		};
		
		confirmDelete(R.string.confirm_delete_plot_msg, confirm);		
	}
	
	
	/**
	 * Species selector has its own activity and workflow.  If it's enabled for
	 * this implementation, it should have a field with an owner of tree.species.
	 * Since Activities with results can only be started from within other 
	 * activities, this is created here, and applied to the view contained by the
	 * field class
	 */
	private void setupSpeciesSelector() {

		OnClickListener speciesClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent speciesSelector = new Intent(App.getInstance(), SpeciesListDisplay.class);
				startActivityForResult(speciesSelector, SPECIES_SELECTOR);
			}
		};

		for (FieldGroup group : App.getFieldManager().getFieldGroups()) {
			for (Entry<String, Field> fieldEntry : group.getFields().entrySet()) {
				Field field = fieldEntry.getValue();
				if (field.owner != null && field.owner.equals("tree.species")) {
					speciesField = field;
					speciesField.attachClickListener(speciesClickListener);
				}
			}
		}
				
	}

	/**
	 * By default cancel() will finish the activity
	 */
	public void cancel() {
		cancel(true);
	}
	
	/**
	 * Cancel the editing and return to the view profile, unchanged.
	 * @param doFinish - if the back button was pushed, finished will
	 * be called for you
	 */
	public void cancel(boolean doFinish) {
		// If the user cancels this activity, but has actually already 
		// changed the photo, we'll consider than an edit so the calling
		// activity will know to refresh the dirty photo
		if (this.photoHasBeenChanged) {
			setResultOk(plot);
		} else {
			setResult(RESULT_CANCELED);
		}
		
		if (doFinish) {
			finish();
		}
	}

	private void save() {
		saveDialog = ProgressDialog.show(this, "", "Saving...", true);

		try {

			for (FieldGroup group : App.getFieldManager().getFieldGroups()) {
				group.update(plot);
			}

			RequestGenerator rg = new RequestGenerator();

			RestHandler responseHandler = new RestHandler<Plot>(new Plot()) {
				@Override
				public void dataReceived(Plot updatedPlot) {
					// The tree was updated, so return to the info page, and bring along
					// the data for the new plot, which was the response from the update
					setResultOk(updatedPlot);
					saveDialog.dismiss();
					finish();
				}
				
				@Override
				protected void handleFailureMessage(Throwable e, String responseBody) {
					Log.e("REST", responseBody);
					saveDialog.dismiss();
					Toast.makeText(App.getInstance(), "Could not save tree!", Toast.LENGTH_SHORT).show();
					Log.e(App.LOG_TAG, "Could not save tree", e);
				}
			};
 
			// check if we are adding a new tree or editing an existing one.
			if ((getIntent().getStringExtra("new_tree") != null) &&
					getIntent().getStringExtra("new_tree").equals("1")) {
				rg.addTree(App.getInstance(), plot, responseHandler);
			} else {			
				rg.updatePlot(App.getInstance(), plot.getId(), plot, responseHandler);
			}
		} catch (Exception e) {
			Log.e(App.LOG_TAG, "Could not save edited plot info", e);
			Toast.makeText(App.getInstance(), "Could not save tree info",
					Toast.LENGTH_LONG).show();
			saveDialog.dismiss();
		}
	}

	/**
	 * Set the result code to OK and set the updated plot as an intent extra
	 * @param updatedPlot
	 */
	private void setResultOk(Plot updatedPlot) {
		Intent resultIntent = new Intent();
		resultIntent.putExtra("plot", updatedPlot.getData().toString());
		setResult(RESULT_OK, resultIntent);
	}
	


    @Override
    public void onBackPressed() {
    	cancel(false);
        super.onBackPressed();
    }
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  switch(requestCode) { 
		  	case SPECIES_SELECTOR: 
		  		if (resultCode == Activity.RESULT_OK) {
		  			CharSequence speciesJSON = data.getCharSequenceExtra("species");
		  			if (speciesJSON != null && !speciesJSON.equals(null)) {
		  				Species species = new Species();
		  				try {
		  					
							species.setData(new JSONObject(speciesJSON.toString()));
							speciesField.setValue(species);
							
						} catch (JSONException e) {
							String msg = "Unable to retrieve selected species";
							Log.e(App.LOG_TAG, msg, e);
							Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
						}
		  			}
		  		}
		  		break; 
		  	case TREE_MOVE:
		  		if (resultCode == Activity.RESULT_OK) {
				  	try {
						plot.setData(new JSONObject(data.getStringExtra("plot")));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					plotLocation = getPlotLocation(plot);
					showPositionOnMap();
		  		}
		  		break;
		  	case PHOTO_USING_CAMERA_RESPONSE:
		  		if (resultCode == RESULT_OK) {
		  			changePhotoUsingCamera();
		  		}
		  		break;
		  	case PHOTO_USING_GALLERY_RESPONSE:
		  		if (resultCode == RESULT_OK) {
		  			changePhotoUsingGallery(data);
		  		}					
		  		break;		  		
	  }
  		
    } 
	
	public void saveEdit(View view) {
		save();
	}
	
	public void cancelEdit(View view) {
		cancel();
	}
	
	
	/* Photo Editing Functions
	 * TODO: refactor, see PhotoActivity
	 **/		
	// Bind your change photo button to this handler.
	public void changeTreePhoto(View view) {
		Log.d("PHOTO", "changePhoto");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setNegativeButton(R.string.use_camera, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   	mImageCaptureUri = Uri.fromFile(
	        	   		new File(
	        	   			Environment.getExternalStorageDirectory(),
	        	   			"otm_tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg"
	        	   	));
	        	   	Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);                     
	                intent.putExtra("return-data", true);
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
	
	// This function is called at the end of the whole camera process. You might
	// want to call your rc.submit method here, or store the bm in a class level
	// variable.
	protected void submitBitmap(Bitmap bm) {
		RequestGenerator rc = new RequestGenerator();
				
		try {
			savePhotoDialog = ProgressDialog.show(this, "", "Saving Photo...", true);
			rc.addTreePhoto(App.getInstance(), plot.getId(), bm, addTreePhotoHandler);
		} catch (JSONException e) {
			Log.e(App.LOG_TAG, "Error updating tree photo.", e);
			savePhotoDialog.dismiss();
		}
	}
	
	protected void changePhotoUsingCamera() {
		Bitmap bm = retrieveBitmapFromCamera(mImageCaptureUri);
		if (bm != null) {submitBitmap(bm);}
	}
	protected void changePhotoUsingGallery(Intent data) {
		Uri selectedImage = data.getData();
        Bitmap bm = retrieveBitmapFromGallery(selectedImage);
        submitBitmap(bm);
	}
	protected Bitmap retrieveBitmapFromCamera(Uri mImageUri) {
	    this.getContentResolver().notifyChange(mImageUri, null);
	    ContentResolver cr = this.getContentResolver();
	    Bitmap bitmap;
	    try
	    {
	        bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
	        return bitmap;
	    }
	    catch (Exception e)
	    {
	        Toast.makeText(this, "Failed to load image from camera", Toast.LENGTH_SHORT).show();
	        Log.d(App.LOG_TAG, "Failed to load image from camera", e);
	        return null;
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
        Bitmap fullSizeBitmap = BitmapFactory.decodeFile(filePath);
        return fullSizeBitmap;
	}	
	/* End of photo stuff*/
}
