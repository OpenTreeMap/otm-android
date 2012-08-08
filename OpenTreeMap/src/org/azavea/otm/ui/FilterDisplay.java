package org.azavea.otm.ui;

import java.util.HashMap;

import org.azavea.otm.R;
import org.azavea.otm.adapters.SpeciesAdapter;
import org.azavea.otm.data.Species;
import org.azavea.otm.filters.BooleanFilter;
import org.azavea.otm.filters.MapFilter;
import org.azavea.otm.filters.RangeFilter;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FilterDisplay extends Activity{
    
    private HashMap<String, MapFilter> filters = new HashMap<String, MapFilter>();
    
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_activity);
        
        LinearLayout list = (LinearLayout)findViewById(R.id.filter_list);
        
        addToggleFilter(new BooleanFilter("filter_missing_tree", false, "Missing Tree"), list);
        addToggleFilter(new BooleanFilter("filter_missing_plot", false, "Missing Plot"), list);
        addToggleFilter(new BooleanFilter("filter_missing_thing", false, "Missing Thing"), list);
        addRangeFilter(new RangeFilter("filter_bip", "Diameter Z"), list);
        addRangeFilter(new RangeFilter("filter_bop", "Diameter X"), list);
        
    }
	
	public void onFinish(View view) {
		
	}
	
	public void onCancel(View view) {
		finish();
	}
	
	public void onClear(View view) {
		
	}
	
	private void addRangeFilter(RangeFilter filter, LinearLayout list) {
        LayoutInflater inflater = ((Activity)this).getLayoutInflater();
        View rangeControl = inflater.inflate(R.layout.filter_range_control, null);
        ((TextView)rangeControl.findViewById(R.id.filter_label)).setText(filter.label);
        rangeControl.setTag(filter);
        list.addView(rangeControl);
	}

	private void addToggleFilter(final BooleanFilter filter, LinearLayout list) {
        LayoutInflater inflater = ((Activity)this).getLayoutInflater();
        View toggle = inflater.inflate(R.layout.filter_toggle_control, null);
        ((TextView)toggle.findViewById(R.id.filter_label)).setText(filter.label);
        toggle.setTag(filter);
        list.addView(toggle);
	}
	
	private void addListFilter() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select a Species");
		 
		Species species[] = null;
		try {
			species = new Species[] {
				new Species(1, "Treeus Maximus", "Maximum Tree"),
				new Species(2, "Treeus Minimus", "Minimum Tree"),
				new Species(3, "Treeus Basicus", "Basic Tree"),
			 };
		} catch (JSONException e) {
			e.printStackTrace();
		}
		 
		 SpeciesAdapter adapter = new SpeciesAdapter(this, R.layout.species_list_row, species);
		 builder.setAdapter(adapter,  new  DialogInterface.OnClickListener() {
			 public void onClick(DialogInterface dialog, int pos) {
			     //selection processing code
			
			 }});
			 builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			     public void onClick(DialogInterface dialog, int whichButton) {
			
			     }
			 });
		 AlertDialog dialog=builder.create();
		 dialog.getListView(); 
		 dialog.show();
	}
}
