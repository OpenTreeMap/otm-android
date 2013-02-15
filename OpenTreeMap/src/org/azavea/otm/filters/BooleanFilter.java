package org.azavea.otm.filters;

import org.azavea.otm.R;

import com.loopj.android.http.RequestParams;

import android.view.View;
import android.widget.ToggleButton;

public class BooleanFilter extends BaseFilter {
	public boolean active;
	private String trueval = "";
	

	public BooleanFilter(String key, String label, String trueval) {
		initialize(key, label, false, trueval);
	}
	
	public BooleanFilter(String key, String label, boolean active, String trueval) {
		initialize(key, label, active, trueval);
	}
	
	private void initialize(String key, String label, boolean active, String trueval) {
		this.key = key;
		this.active = active;
		this.label = label;
		this.trueval = trueval;
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
	public void addToRequestParams(RequestParams rp) {
		if (trueval.equals("")) {
			rp.put(key, Boolean.toString(active));
		} else {
			rp.put(key,  trueval);
		}
	}
}
