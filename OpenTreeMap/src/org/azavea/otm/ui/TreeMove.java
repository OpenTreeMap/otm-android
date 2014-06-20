package org.azavea.otm.ui;

import java.util.ArrayList;
import org.azavea.otm.R;
import org.azavea.otm.data.Geometry;
import org.json.JSONException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.joelapenna.foursquared.widget.SegmentedButton;
import com.joelapenna.foursquared.widget.SegmentedButton.OnClickListenerSegmentedButton;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class TreeMove extends TreeDisplay{
    public void onCreate(Bundle savedInstanceState) {
    	mapFragmentId = R.id.moveable_marker_map;
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_tree_move);
    	setUpMapIfNeeded();
    	showPositionOnMap();
    	plotMarker.setDraggable(true);
    	setUpBasemapControls();
    }
  
    public void submitTreeMove(View view) {
    	LatLng position = plotMarker.getPosition();
    	try {
    		Geometry g = plot.getGeometry();
    	   	g.setY(position.latitude);
    	   	g.setX(position.longitude);
    	   	plot.setGeometry(g);
     	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    	Intent editPlot = new Intent(this, TreeEditDisplay.class);
    	editPlot.putExtra("plot", plot.getData().toString());
    	setResult(RESULT_OK, editPlot);
    	finish();
    }
    
    public void setUpBasemapControls() {
    	// Create the segmented buttons
        SegmentedButton buttons = (SegmentedButton)findViewById(R.id.basemap_controls);
        buttons.clearButtons();
        
        ArrayList<String> buttonNames = new ArrayList<>();
        buttonNames.add("map");
        buttonNames.add("satellite");
        buttonNames.add("hybrid");
        buttons.addButtons(buttonNames.toArray(new String[buttonNames.size()]));
        
        buttons.setOnClickListener((int index) -> {
            switch (index) {
            case 0:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case 1:
                   mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case 2:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            }
        });
    }
}
