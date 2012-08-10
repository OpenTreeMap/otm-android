package org.azavea.otm.filters;

import org.azavea.otm.R;

import android.view.View;
import android.widget.ToggleButton;

public class BooleanFilter extends BaseFilter {
	public boolean active;
	

	public BooleanFilter(String key, String label) {
		initialize(key, label, false);
	}
	
	public BooleanFilter(String key, String label, boolean active) {
		initialize(key, label, active);
	}
	
	private void initialize(String key, String label, boolean active) {
		this.key = key;
		this.active = active;
		this.label = label;
	}
	
	@Override
	public String toQueryStringParam() {
		return key + "=" + Boolean.toString(active);
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
}
