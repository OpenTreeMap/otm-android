package org.azavea.otm.ui;

import java.util.Map.Entry;

import org.azavea.otm.App;
import org.azavea.otm.Field;
import org.azavea.otm.FieldGroup;
import org.azavea.otm.R;
import org.azavea.otm.data.EditEntryContainer;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.Species;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.RestHandler;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

public class TreeEditDisplay extends TreeDisplay {

	protected static final int SPECIES_SELECTOR = 0;
	private Field speciesField;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
							setResult(RESULT_OK);
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
			case R.id.cancel_edits:
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
	  } 
	}
}
