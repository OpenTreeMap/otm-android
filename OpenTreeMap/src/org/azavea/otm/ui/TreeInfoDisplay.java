package org.azavea.otm.ui;

import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TreeInfoDisplay extends Activity{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_view_activity);
        
        try {
        	Plot plot = new Plot();
			plot.setData(new JSONObject(getIntent().getStringExtra("plot")));
			
			setTreeHeaderValues(plot);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	private void setTreeHeaderValues(Plot plot) throws JSONException {
		((TextView)findViewById(R.id.address)).setText(plot.getAddress());
		((TextView)findViewById(R.id.species)).setText(plot.getTree().getSpeciesName());
		((TextView)findViewById(R.id.updated_on)).setText("Last updated on " + plot.getLastUpdated());
		((TextView)findViewById(R.id.updated_by)).setText("By " + plot.getLastUpdatedBy());
	}
}
