package org.azavea.otm.ui;

import java.io.UnsupportedEncodingException;
import java.util.Currency;

import org.azavea.otm.App;
import org.azavea.otm.FieldGroup;
import org.azavea.otm.R;
import org.azavea.otm.data.Geometry;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.Tree;
import org.azavea.otm.rest.RequestGenerator;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TreeMove extends TreeDisplay{
    public void onCreate(Bundle savedInstanceState) {
    	mapFragmentId = R.id.moveable_marker_map;
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_tree_move);
    	setUpMapIfNeeded();
    	showPositionOnMap();
    	plotMarker.setDraggable(true);
    }
  
    public void submitTreeMove(View view) {
    	LatLng position = plotMarker.getPosition();
    	try {
    		Geometry g = plot.getGeometry();
    	   	g.setLat(position.latitude);
    	   	g.setLon(position.longitude);
    	   	plot.setGeometry(g);
     	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    	Intent editPlot = new Intent(this, TreeEditDisplay.class);
    	editPlot.putExtra("plot", plot.getData().toString());
    	setResult(RESULT_OK, editPlot);
    	finish();
    }
}
