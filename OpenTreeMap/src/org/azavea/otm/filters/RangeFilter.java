package org.azavea.otm.filters;

import org.azavea.otm.R;

import com.loopj.android.http.RequestParams;

import android.view.View;
import android.widget.EditText;

public class RangeFilter extends BaseFilter {
	final private double DEFAULT = 0;
	private double min = DEFAULT;
	private double max = DEFAULT;
	
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
	public String toQueryStringParam() {
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
	public boolean isActive() {
		return (min > DEFAULT || max > DEFAULT) ? true : false;
	}

	@Override
	public void updateFromView(View view) {
		String min = ((EditText)view.findViewById(R.id.min))
				.getText().toString().trim();
		String max = ((EditText)view.findViewById(R.id.max))
				.getText().toString().trim();
		
		if (min != null && !"".equals(min)) {
			this.min = Double.parseDouble(min);
		} else {
			this.min = DEFAULT;
		}
			
		if (max != null && !"".equals(max)) {
			this.max = Double.parseDouble(max);
		} else {
			this.max = DEFAULT;
		}
	}

	@Override
	public void clear() {
		min = DEFAULT;
		max = DEFAULT;
	}
	
	@Override
	public void addToRequestParams(RequestParams rp) {
		if (min > 0) {
			rp.put(minKey(), Double.toString(min));
		}
		if (max > 0) {
			rp.put(maxKey(), Double.toString(max));
		}				
	}
}
