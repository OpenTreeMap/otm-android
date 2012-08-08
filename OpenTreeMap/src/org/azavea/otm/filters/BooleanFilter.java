package org.azavea.otm.filters;

import org.azavea.otm.R;

import android.view.View;
import android.widget.ToggleButton;

public class BooleanFilter extends MapFilter {
	public boolean active;
	
	public BooleanFilter(String key, boolean active, String label) {
		this.key = key;
		this.active = active;
		this.label = label;
	}
	
	@Override
	protected String toQueryStringParam() {
		return key + "=" + Boolean.toString(active);
	}

	@Override
	void updateFromView(View view) {
		this.active = ((ToggleButton)view.findViewById(R.id.active)).isChecked();
	}

	@Override
	boolean isActive() {
		return this.active;
	}
}
