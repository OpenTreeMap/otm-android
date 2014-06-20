package org.azavea.otm.ui;


import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.FilterManager;
import org.azavea.otm.adapters.SpeciesAdapter;
import org.azavea.otm.data.Species;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SpeciesListDisplay extends ListActivity {
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		FilterManager search = App.getFilterManager();
		
		if (search.getSpecies().size() > 0) {
			renderSpeciesList();
		} else {
			search.loadSpeciesList(msg -> {
                if (msg.getData().getBoolean("success")) {
                    renderSpeciesList();
                } else
                      {
                    Toast.makeText(App.getAppInstance(), "Could not get species list",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            });
		}
	}
	
	private void renderSpeciesList() {
		LinkedHashMap<Integer, Species> list = App.getFilterManager().getSpecies();
		Species[] species = new Species[list.size()];
		int i = 0;
		for (Map.Entry<Integer,Species> entry : list.entrySet()) {
			species[i] = entry.getValue();
			i++;
		}
		// Sort by common name
		Arrays.sort(species);
		
		// Bind the custom adapter to the view
		SpeciesAdapter adapter = new SpeciesAdapter(this, R.layout.species_list_row, 
				species);
		Log.d(App.LOG_TAG, list.size() + " species loaded");
		setListAdapter(adapter);
		setContentView(R.layout.species_list_selector);
		setupFiltering(adapter);

	}
	
	private void setupFiltering(final SpeciesAdapter adapter) {
		EditText filterEditText = (EditText) findViewById(R.id.species_filter_text);
		setKeyboardChangeEvents(adapter, filterEditText);
		setTextWatcherEvents(adapter, filterEditText); 		
	}

	/**
	 * Listen on events of the filter text box to pass along filter text
	 * and invalidate the current view
	 */
	private void setTextWatcherEvents(final SpeciesAdapter adapter,
			EditText filterEditText) {
		filterEditText.addTextChangedListener(new TextWatcher() {
		   @Override
		    public void onTextChanged(CharSequence s, int start, int before,
		    		int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				adapter.getFilter().filter(s);
				adapter.notifyDataSetChanged();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			});
	}

	/**
	 *  Listen on events the keyboard emits when finished editing
	 *  ('Done' and 'Next' are the ENTER event).
	 *  The Keyboard ENTER event on a ListActivity will re-render
	 *  the list, so the current view must be invalidated
	 */
	private void setKeyboardChangeEvents(final SpeciesAdapter adapter,
			EditText filterEditText) {
		filterEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
            {
                adapter.notifyDataSetChanged();
            }
            return false;
        });
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Species selection = (Species)l.getItemAtPosition(position);
		Intent result = new Intent();
		
		result.putExtra("species", selection.getData().toString());
		
		setResult(RESULT_OK, result);
		finish();
	}	    
 }
