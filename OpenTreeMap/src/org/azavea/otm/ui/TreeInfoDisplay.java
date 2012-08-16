package org.azavea.otm.ui;

import org.azavea.otm.App;
import org.azavea.otm.FieldGroup;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TreeInfoDisplay extends Activity{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_view_activity);
        LinearLayout fieldList = (LinearLayout)findViewById(R.id.field_list);
        LayoutInflater layout = ((Activity)this).getLayoutInflater();
        
        try {
        	Plot plot = new Plot();
			plot.setData(new JSONObject(getIntent().getStringExtra("plot")));
			setTreeHeaderValues(plot);
			
			for (FieldGroup group : App.getFieldManager().getFieldGroups()) {
				fieldList.addView(group.renderForDisplay(layout, plot));
			}
		} catch (JSONException e) {
			Toast.makeText(this, "Could not retrieve Tree information", 
					Toast.LENGTH_SHORT).show();
			Log.e(App.LOG_TAG, "Failed to create tree view", e);
		}
    }

	private void setTreeHeaderValues(Plot plot) throws JSONException {
		setText(R.id.address, plot.getAddress());
		setText(R.id.species, plot.getTree().getSpeciesName());
		setText(R.id.updated_on, "Last updated on " + plot.getLastUpdated());
		setText(R.id.updated_by, "By " + plot.getLastUpdatedBy());
	}
	
	private void setText(int resourceId, String text) {
		// Only set the text if it exists, letting the layout define default text
		if (text != null &&  !"".equals(text)) {
			((TextView)findViewById(resourceId)).setText(text);
		}
	}
}
