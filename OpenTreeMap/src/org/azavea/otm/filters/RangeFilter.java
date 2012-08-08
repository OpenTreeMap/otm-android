package org.azavea.otm.filters;

import org.azavea.otm.R;

import android.view.View;
import android.widget.EditText;

public class RangeFilter extends MapFilter {
	private double min = 0;
	private double max = 0;
	
	public RangeFilter(String key, String label) {
		this.key = key;
		this.label = label;
	}
	
	public double getMin() {
		return min;
	}
	
	public double getMax() {
		return max;
	}
	
	private String minKey() {
		return key + "_min";
	}
	
	private String maxKey() {
		return key + "_max";
	}

	@Override
	protected String toQueryStringParam() {
		String params = "";
		if (min > 0) {
			params += minKey() + "=" + Double.toString(min); 
		}
		if (max > 0) {
			params += maxKey() + "=" + Double.toString(max);
		}
		return params;
	}

	@Override
	boolean isActive() {
		return (min > 0 || max > 0) ? true : false;
	}

	@Override
	void updateFromView(View view) {
		String min = ((EditText)view.findViewById(R.id.min))
				.getText().toString().trim();
		if (min != null && !"".equals(min)) {
			this.min = Double.parseDouble(min);
		}
	}
}
