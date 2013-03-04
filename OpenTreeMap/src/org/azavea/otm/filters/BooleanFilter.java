package org.azavea.otm.filters;

import org.azavea.otm.R;

import com.loopj.android.http.RequestParams;

import android.view.View;
import android.widget.ToggleButton;

public class BooleanFilter extends BaseFilter {
	public boolean active;
	private String cqlTrueval = "";
	private String nearestPlotTrueval = "";
	

	public BooleanFilter(String cqlKey, String nearestPlotKey, String label, String cqlTrueval, String nearestPlotTrueval) {
		initialize(cqlKey, nearestPlotKey, label, cqlTrueval, nearestPlotTrueval, false);
	}
	
	public BooleanFilter(String cqlKey, String nearestPlotKey, String label, String cqlTrueval, String nearestPlotTrueval, boolean active) {
		initialize(cqlKey, nearestPlotKey, label, cqlTrueval, nearestPlotTrueval, active);
	}
	
	private void initialize(String cqlKey, String nearestPlotKey, String label,  String cqlTrueval, String nearestPlotTrueval, boolean active) {
		this.cqlKey = cqlKey;
		this.nearestPlotKey = nearestPlotKey;
		this.active = active;
		this.label = label;
		this.cqlTrueval = cqlTrueval;
		this.nearestPlotTrueval = nearestPlotTrueval;
	}

	@Override
	public void updateFromView(View view) {
		this.active = ((ToggleButton)view.findViewById(R.id.active)).isChecked();
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	@Override
	public void clear() {
		active = false;
	}
	
	@Override
	public void addToCqlRequestParams(RequestParams rp) {
		if (cqlTrueval.equals("")) {
			rp.put(cqlKey, Boolean.toString(active));
		} else {
			rp.put(cqlKey,  cqlTrueval);
		}
	}
	
	public void addToNearestPlotRequestParams(RequestParams rp) {
		if (nearestPlotTrueval.equals("")) {
			rp.put(nearestPlotKey, Boolean.toString(active));
		} else {
			rp.put(nearestPlotKey, nearestPlotTrueval);
		}
	}
	
}
