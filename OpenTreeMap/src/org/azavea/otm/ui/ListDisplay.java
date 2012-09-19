package org.azavea.otm.ui;

import java.util.Map;

import org.azavea.lists.InfoList;
import org.azavea.lists.ListObserver;
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
        
        dialog = ProgressDialog.show(this, "", 
                "Loading. Please wait...", true);
		dialog.show();

		processRadioButtonSelection();
        
        update();
    }

	private void processRadioButtonSelection() {
        int selectedRadioButton = radioGroup.getCheckedRadioButtonId();
		switch(selectedRadioButton) {
        	case R.id.radioNearby: infoList = App.getNearbyList();
		   	   					   infoList.addObserver(this);
		   	   					   infoList.setupLocationUpdating(getApplicationContext());
        					   	   listView.setOnItemClickListener(getOnClickListener());
        					   	   break;
//        case R.id.radioPending: infoList = App.getPendingList();
//        	break;
//        case R.id.radioRecent: infoList = App.getRecentList();
//        	break;
        }
	}
	
	public ListView.OnItemClickListener getOnClickListener() {
		return new ListView.OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> a, View v, int i, long l) {
        		Intent viewPlot = new Intent(ListDisplay.this, TreeInfoDisplay.class);
//        		Object[] plots = infoList.getListValues();
//        		Plot selectedPlot = (Plot)plots[i];
        		Plot selectedPlot = ((DisplayablePlot)a.getItemAtPosition(i)).getPlot();
        		viewPlot.putExtra("plot", selectedPlot.getData().toString());
        		ListDisplay.this.startActivity(viewPlot);
        	}
        };
	}
	
	public void update() {
		dialog.show();

        adapter = new ArrayAdapter<DisplayableModel>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, infoList.getDisplayValues());
        
		// Assign adapter to ListView
		listView.setAdapter(adapter);

		adapter.notifyDataSetChanged();
		dialog.hide();
	}
}

