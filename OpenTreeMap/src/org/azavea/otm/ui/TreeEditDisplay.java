package org.azavea.otm.ui;

import org.azavea.otm.App;
import org.azavea.otm.FieldGroup;
import org.azavea.otm.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class TreeEditDisplay extends TreeDisplay{
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.plot_edit_activity);
    	
        LinearLayout fieldList = (LinearLayout)findViewById(R.id.field_list);
        LayoutInflater layout = ((Activity)this).getLayoutInflater();
        
		showPositionOnMap();
		
		for (FieldGroup group : App.getFieldManager().getFieldGroups()) {
			View fieldGroup = group.renderForEdit(layout, plot, currentUser);
			if (fieldGroup != null) {
				fieldList.addView(fieldGroup);
			}
		}
    }
 
}
