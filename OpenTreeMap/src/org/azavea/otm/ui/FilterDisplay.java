package org.azavea.otm.ui;

import org.azavea.otm.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class FilterDisplay extends Activity{
    final private int SIDE_MARGIN = 15;
    
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_activity);
        
        LinearLayout list = (LinearLayout)findViewById(R.id.filter_list);
        addToggleFilter("Missing Tree", false, "filter_missing_tree", list);
        addToggleFilter("Missing Species", true, "filter_missing_tree", list);
    }
	
	private void addToggleFilter(String label, boolean value, String key, 
			LinearLayout list) {

		RelativeLayout container = new RelativeLayout(this);
		
		TextView text = new TextView(this);
		text.setTextAppearance(this, android.R.style.TextAppearance_Medium);
		text.setText(label);
		
		ToggleButton toggle = new ToggleButton(this);
		toggle.setChecked(value);
		
		container.addView(text, getLabelLayout());
		container.addView(toggle, getControlLayout());
		list.addView(container);
	}
	
	private LayoutParams getLabelLayout() {
		RelativeLayout.LayoutParams params = 
				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
						RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		params.setMargins(SIDE_MARGIN, 0, 0, 0);
		
		return params;
	}

	private LayoutParams getControlLayout() {
		RelativeLayout.LayoutParams params = 
				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
						RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		params.setMargins(0, SIDE_MARGIN, 0, 0);
		
		return params;
	}
}
