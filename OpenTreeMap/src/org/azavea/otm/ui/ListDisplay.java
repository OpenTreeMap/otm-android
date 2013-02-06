package org.azavea.otm.ui;

import java.util.ArrayList;

import org.azavea.lists.InfoList;
import org.azavea.lists.ListObserver;
import org.azavea.lists.NearbyList;
import org.azavea.lists.data.DisplayableModel;
import org.azavea.lists.data.DisplayablePlot;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.User;

import com.joelapenna.foursquared.widget.SegmentedButton;
import com.joelapenna.foursquared.widget.SegmentedButton.OnClickListenerSegmentedButton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.util.Log;
import android.view.View;


public class ListDisplay extends Activity implements ListObserver {
	private ListView listView;
	private InfoList infoList;
	private ProgressDialog dialog;
	private ArrayAdapter<DisplayableModel> adapter;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);
        
        // Create the segmented buttons
        SegmentedButton buttons = (SegmentedButton)findViewById(R.id.segmented);
        buttons.clearButtons();
        
        ArrayList<String> buttonNames = new ArrayList<String>();
        buttonNames.add(getString(R.string.toggle_nearby));
        buttonNames.add(getString(R.string.toggle_recent));
        
        if (App.isPendingEnabled()) {
        	buttonNames.add(1, getString(R.string.toggle_pending));
        }
        
        buttons.addButtons(buttonNames.toArray(new String[buttonNames.size()]));
        
        buttons.setOnClickListener(new OnClickListenerSegmentedButton() {
            @Override
            public void onClick(int index) {
            	processRadioButtonSelection(index);
            }
        });
        
        listView = (ListView)findViewById(R.id.listItems);
        
        dialog = ProgressDialog.show(ListDisplay.this, "", 
                "Loading. Please wait...", true);
		
		infoList = App.getNearbyList();
		infoList.addObserver(this);
		infoList.setupLocationUpdating(getApplicationContext());
		listView.setOnItemClickListener(getOnClickListener());
		
		processRadioButtonSelection(0);
        
        update();
    }

	private void processRadioButtonSelection(int index) {
		dialog.show();
        NearbyList nearbyList = (NearbyList)infoList;
        switch(index) {
        	case 0: nearbyList.setFilterRecent(false);
        		    nearbyList.setFilterPending(false);
        			break;
        	case 1: nearbyList.setFilterRecent(true);
        		    nearbyList.setFilterPending(false);
        		    break;
        	case 2: nearbyList.setFilterRecent(false);
        			nearbyList.setFilterPending(true);
        			break;
        }
        nearbyList.update();
	}
	
	public ListView.OnItemClickListener getOnClickListener() {
		return new ListView.OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> a, View v, int i, long l) {
        		Intent viewPlot = new Intent(ListDisplay.this, TreeInfoDisplay.class);
        		
        		Plot selectedPlot = ((DisplayablePlot)a.getItemAtPosition(i)).getPlot();
        		viewPlot.putExtra("plot", selectedPlot.getData().toString());
        		
        		User user = App.getLoginManager().loggedInUser;
        		if (user != null) {
        			viewPlot.putExtra("user", user.getData().toString());
        		} else {
        			// extra "user" will be null, which is handled in the activity.
        		}
        		
        		ListDisplay.this.startActivity(viewPlot);
        	}
        };
	}
	
	public void update() {
        adapter = new ArrayAdapter<DisplayableModel>(this,
                R.layout.simple_list_item, android.R.id.text1, infoList.getDisplayValues());
        
		listView.setAdapter(adapter);

		adapter.notifyDataSetChanged();

		Log.d(App.LOG_TAG, "Hide dialog");
		dialog.hide();
	}
}
