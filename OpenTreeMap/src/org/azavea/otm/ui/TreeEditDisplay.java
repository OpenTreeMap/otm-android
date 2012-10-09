package org.azavea.otm.ui;

import org.azavea.otm.App;
import org.azavea.otm.FieldGroup;
import org.azavea.otm.R;
import org.azavea.otm.data.EditEntryContainer;
import org.azavea.otm.data.Plot;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.RestHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class TreeEditDisplay extends TreeDisplay {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plot_edit_activity);

		LinearLayout fieldList = (LinearLayout) findViewById(R.id.field_list);
		LayoutInflater layout = ((Activity) this).getLayoutInflater();

		showPositionOnMap();

		for (FieldGroup group : App.getFieldManager().getFieldGroups()) {
			View fieldGroup = group.renderForEdit(layout, plot, currentUser);
			if (fieldGroup != null) {
				fieldList.addView(fieldGroup);
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

}
