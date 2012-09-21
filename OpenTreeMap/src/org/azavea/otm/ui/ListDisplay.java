package org.azavea.otm.ui;

import java.util.Map;

import org.azavea.lists.InfoList;
import org.azavea.lists.ListObserver;
import org.azavea.lists.NearbyList;
import org.azavea.lists.data.DisplayableModel;
import org.azavea.lists.data.DisplayablePlot;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.util.Log;
import android.view.View;


public class ListDisplay extends Activity implements ListObserver {
	private ListView listView;
	private RadioGroup radioGroup;
	private InfoList infoList;
	private ProgressDialog dialog;
	private ArrayAdapter<DisplayableModel> adapter;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);
        
        listView = (ListView)findViewById(R.id.listItems);
        radioGroup = (RadioGroup)findViewById(R.id.listOptions);
        
        dialog = ProgressDialog.show(ListDisplay.this, "", 
                "Loading. Please wait...", true);
		
		infoList = App.getNearbyList();
		infoList.addObserver(this);
		infoList.setupLocationUpdating(getApplicationContext());
		listView.setOnItemClickListener(getOnClickListener());
		
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				processRadioButtonSelection();
			}
		});
		
		processRadioButtonSelection();
        
        update();
    }

	private void processRadioButtonSelection() {
		dialog.show();
		int selectedRadioButton = radioGroup.getCheckedRadioButtonId();
        NearbyList nearbyList = (NearbyList)infoList;
        switch(selectedRadioButton) {
        	case R.id.radioNearby: nearbyList.setFilterRecent(false);
        						   nearbyList.setFilterPending(false);
        					   	   break;
        	case R.id.radioPending: nearbyList.setFilterRecent(false);
        							nearbyList.setFilterPending(true);
        							break;
        	case R.id.radioRecent: nearbyList.setFilterRecent(true);
        					   	   nearbyList.setFilterPending(false);
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
        		ListDisplay.this.startActivity(viewPlot);
        	}
        };
	}
	
	public void update() {
        adapter = new ArrayAdapter<DisplayableModel>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, infoList.getDisplayValues());
        
		listView.setAdapter(adapter);

		adapter.notifyDataSetChanged();

		Log.d(App.LOG_TAG, "Hide dialog");
		dialog.hide();
	}
}
