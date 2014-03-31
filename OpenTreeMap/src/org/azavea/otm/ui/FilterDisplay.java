package org.azavea.otm.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Species;
import org.azavea.otm.filters.BooleanFilter;
//import org.azavea.otm.filters.ChoiceFilter;
import org.azavea.otm.filters.SpeciesFilter;
import org.azavea.otm.filters.BaseFilter;
import org.azavea.otm.filters.RangeFilter;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class FilterDisplay extends Activity{
	
    final private int SPECIES_SELECTOR = 1;
    private View speciesFilter;
    private LinearLayout filter_list;
    
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_activity);
		
		//filter_list = (LinearLayout)findViewById(R.id.filter_list);
		//createFilterUI(App.getFilterManager().getFilters(), filter_list);
    }
	
	public void onComplete(View view) {
		// Update any active filters from the view
		
		int filterCount = filter_list.getChildCount();
		for (int i=0; i < filterCount; i++) {
			View filter_view = filter_list.getChildAt(i);
			String filterKey = filter_view.getTag(R.id.filter_key).toString();
			App.getFilterManager().updateFilterFromView(filterKey, filter_view);
		}
		setResult(RESULT_OK);
		finish();
	}
	
	public void onCancel(View view) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	public void onClear(View clearButton) {
		resetFilterUI(filter_list);
		resetFilters(App.getFilterManager().getFilters());
		resetSpecies();
	}
	
	/***
	 * Notify all filter that they should clear there state to off
	 * @param filters
	 */
	private void resetFilters(LinkedHashMap<String, BaseFilter> filters) {
		for (Entry<String, BaseFilter> filter : filters.entrySet()){
			filter.getValue().clear();
		}
	}

	private void resetFilterUI(ViewGroup group) {
		// Recursively clear all text and toggle elements
		for (int i = 0, count = group.getChildCount(); i < count; ++i) {
		    View view = group.getChildAt(i);
		    if (view instanceof EditText) {
		        ((EditText)view).setText("");
		    } else if (view instanceof ToggleButton) {
		    	((ToggleButton)view).setChecked(false);
		    } else if (view instanceof ViewGroup) {
		    	resetFilterUI((ViewGroup)view);
		    } else if (view instanceof Button) {
		    	Object label = view.getTag();
		    	if (label != null) {
		    		((Button)view).setText(label.toString());
		    	}
		    }
		}
	}
	
	private void createFilterUI(LinkedHashMap<String,BaseFilter> filters, 
			LinearLayout parent) {
		
		LayoutInflater layout = ((Activity)this).getLayoutInflater();
		for (Map.Entry<String, BaseFilter> entry : filters.entrySet()) {
			BaseFilter filter = entry.getValue();
			View view = null;
			if (filter instanceof BooleanFilter) {
				view = makeToggleFilter((BooleanFilter)filter, layout);
			} else if (filter instanceof RangeFilter) {
				view = makeRangeFilter((RangeFilter)filter, layout);
			} else if (filter instanceof SpeciesFilter) {
				view = makeListFilter((SpeciesFilter)filter, layout);
			//} else if (filter instanceof ChoiceFilter) {
			//	view = makeChoiceFilter((ChoiceFilter)filter, layout);
			} else {
				Log.e(App.LOG_TAG, "Invalid filter specified, unable to create UI");
			}
	        view.setTag(R.id.filter_key, filter.cqlKey);  // this is a bit arbitrary...
	        parent.addView(view);
		}
	}
	
	private View makeRangeFilter(RangeFilter filter, LayoutInflater layout) {
		RangeFilter range = (RangeFilter)filter;
        View rangeControl = layout.inflate(R.layout.filter_range_control, null);
        ((TextView)rangeControl.findViewById(R.id.filter_label)).setText(range.label);
        if (filter.isActive()) {
        	((EditText)rangeControl.findViewById(R.id.min))
        		.setText(Double.toString(range.getMin()));
        	((EditText)rangeControl.findViewById(R.id.max))
    			.setText(Double.toString(range.getMax()));
        }
        return rangeControl;
	}

	private View makeToggleFilter(final BooleanFilter filter, LayoutInflater layout) {
        View toggle = layout.inflate(R.layout.filter_toggle_control, null);
        ((TextView)toggle.findViewById(R.id.filter_label)).setText(filter.label);
        if (filter.isActive()) {
        	((ToggleButton)toggle.findViewById(R.id.active)).setChecked(true);
        }
        return toggle;
	}
	
	private View makeListFilter(SpeciesFilter filter, LayoutInflater layout) {
        speciesFilter = layout.inflate(R.layout.filter_species_control, null);
        Button button = ((Button)speciesFilter.findViewById(R.id.species_filter));
        if (filter.isActive()) {
        	updateSpecies(filter, filter.species);
        } else {
        	resetSpecies(filter);
        }
        button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent speciesSelector = new Intent(App.getAppInstance(), SpeciesListDisplay.class);
				startActivityForResult(speciesSelector, SPECIES_SELECTOR);
			}
		});
        return speciesFilter;
	}

//	private View makeChoiceFilter(final ChoiceFilter filter, LayoutInflater layout) {
//		View choiceLayout = layout.inflate(R.layout.filter_choice_control, null);
//		final Button choiceButton = (Button)choiceLayout.findViewById(R.id.choice_filter);
//
//		choiceButton.setText(filter.getSelectedLabelText());
//		
//		// Tag will hold the default label for clear events
//		choiceButton.setTag(filter.label);
//		
//		choiceButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				AlertDialog dialog = new AlertDialog.Builder(choiceButton.getContext())
//					.setTitle(filter.label)
//					.setSingleChoiceItems(filter.choices.getItems().toArray(new String[0]),
//						filter.getSelectedIndex(), 
//						new DialogInterface.OnClickListener() {
//					
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							filter.setSelectedIndex(which);
//							choiceButton.setText(filter.getSelectedLabelText());
//							dialog.dismiss();
//						}
//					})
//				.create();
//				
//				dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Clear", new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						filter.setSelectedIndex(-1);
//						choiceButton.setText(filter.getSelectedLabelText());
//						
//					}
//				});
//				
//				dialog.show();
//				
//			}
//		});
//		
//		return choiceLayout;
//	}
	
	private void resetSpecies(BaseFilter filter) {
		updateSpecies(filter, null);
	}
	
	private void resetSpecies() {
		resetSpecies(null);
	}
	
	private void updateSpecies(Species species) {
		updateSpecies(null,  species);
	}
	
	private void updateSpecies(BaseFilter filter, Species species) {
		if (filter == null) {
			String key = speciesFilter.getTag(R.id.filter_key).toString();
			filter = App.getFilterManager().getFilter(key);
		}
		String name = "Not filtered";
		if (species != null) {
            name = species.getCommonName();
		}
        Button button = ((Button)speciesFilter.findViewById(R.id.species_filter));
		speciesFilter.setTag(R.id.species_id, species);
		button.setText(filter.label + ": " + name);
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  switch(requestCode) { 
	  	case (SPECIES_SELECTOR) : { 
	  		if (resultCode == Activity.RESULT_OK) {
	  			CharSequence speciesJSON = data.getCharSequenceExtra("species");
	  			if (speciesJSON != null && !speciesJSON.equals(null)) {
	  				Species species = new Species();
	  				try {
	  					
						species.setData(new JSONObject(speciesJSON.toString()));
						updateSpecies(species);	
						
					} catch (JSONException e) {
						String msg = "Unable to retrieve selected species";
						Log.e(App.LOG_TAG, msg, e);
						Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
					}	
	  			}
	  		} 
	  		break; 
	    } 
	  } 
	}
}
