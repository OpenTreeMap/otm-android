package org.azavea.otm.ui;

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

import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.graphics.Bitmap;

public class TreeEditDisplay extends TreeDisplay {

	protected static final int SPECIES_SELECTOR = 0;
	protected static final int TREE_PHOTO = 1;
	
	private Field speciesField;

	ProgressDialog deleteDialog = null;
	
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
		};
		
		protected void handleFailureMessage(Throwable e, String responseBody) {
			Log.e("addTreePhoto", "addTreePhotoHandler.handleFailureMessage");
			Log.e("addTreePhoto", "e.toString " + e.toString());
			Log.e("addTreePhoto", "responseBody: " + responseBody);
			Log.e("addTreePhoto", "e.getMessage: " + e.getMessage());
			Log.e("addTreePhoto", "e.getCause: " + e.getCause());
			e.printStackTrace();
			Toast.makeText(App.getInstance(), "The tree photo was added.", Toast.LENGTH_LONG).show();					
		};
	};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initializeEditPage();
	}


	private void initializeEditPage() {
		if (plot == null) {
			finish();
		}
		setContentView(R.layout.plot_edit_activity);

		LinearLayout fieldList = (LinearLayout) findViewById(R.id.field_list);
		LayoutInflater layout = ((Activity) this).getLayoutInflater();

		showPositionOnMap();

		// Add all the fields to the display for edit mode
		for (FieldGroup group : App.getFieldManager().getFieldGroups()) {
			View fieldGroup = group.renderForEdit(layout, plot, currentUser);
			if (fieldGroup != null) {
				fieldList.addView(fieldGroup);
			}
		}
		
		setupSpeciesSelector();
		
		setupChangePhotoButton(layout, fieldList);
		
		setupDeleteButtons(layout, fieldList);		
	}
	
	/**
	 * Delete options for tree and plot are available under certain situations
	 * as reported from the /plot API endpoint as attributes of a plot/user
	 * combo.
	 */
	private void setupDeleteButtons(LayoutInflater layout, LinearLayout fieldList) {
		View actionPanel = layout.inflate(R.layout.plot_edit_delete_buttons, null);
		int plotVis = View.GONE;
		int treeVis = View.GONE;
		
		try {
			if (plot.canDeletePlot()) {
				plotVis = View.VISIBLE;
			}
			if (plot.canDeleteTree()) {
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
		View thePanel = layout.inflate(R.layout.plot_edit_photo_button, null);
		fieldList.addView(thePanel);
		
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

	public void changeTreePhoto(View view) {
		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, TREE_PHOTO);
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
	 * Cancel the editing and return to the view profile, unchanged
	 */
	public void cancel() {
		setResult(RESULT_CANCELED);
		finish();
	}

	private void save() {
		final ProgressDialog dialog = ProgressDialog.show(this, "",
				"Saving...", true, true);

		try {

			for (FieldGroup group : App.getFieldManager().getFieldGroups()) {
				group.update(plot);
			}
					
			RequestGenerator rg = new RequestGenerator();
			rg.updatePlot(App.getInstance(), plot.getId(), plot,
					new RestHandler<Plot>(new Plot()) {
						@Override
						public void dataReceived(Plot updatedPlot) {
							Intent resultIntent = new Intent();
							
							// The tree was updated, so return to the info page, and bring along
							// the data for the new plot, which was the response from the update
							resultIntent.putExtra("plot", updatedPlot.getData().toString());
							setResult(RESULT_OK, resultIntent);
							finish();
						}

					});

		} catch (Exception e) {
			Log.e(App.LOG_TAG, "Could not save edited plot info", e);
			Toast.makeText(App.getInstance(), "Could not save tree info",
					Toast.LENGTH_LONG).show();
		} finally {
			dialog.dismiss();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_edit_tree_display, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.save_edits:
				save();
				break;
			case R.id.cancel_edits:deleteDialog.dismiss();
				cancel();
				break;
		}
		return true;
	}

	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  switch(requestCode) { 
	  	case (SPECIES_SELECTOR) : { 
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
	    } 
	  	case (TREE_PHOTO) : {
		  	if (resultCode == Activity.RESULT_OK) {
		  		Bitmap bm = (Bitmap) data.getExtras().get("data");
		  		RequestGenerator rc = new RequestGenerator();
				try {
					rc.addTreePhoto(App.getInstance(), plot.getId(), bm, addTreePhotoHandler);
				} catch (JSONException e) {
					Log.e(App.LOG_TAG, "Error updating tree photo.", e);
				}
	  		}
	  		break; 
	    } 
	  }
	}
}
