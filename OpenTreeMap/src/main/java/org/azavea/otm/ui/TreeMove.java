package org.azavea.otm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.joelapenna.foursquared.widget.SegmentedButton;

import org.azavea.helpers.Logger;
import org.azavea.otm.R;
import org.azavea.otm.data.Geometry;
import org.json.JSONException;

public class TreeMove extends TreeDisplay {
    public void onCreate(Bundle savedInstanceState) {
        mapFragmentId = R.id.moveable_marker_map;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_move);
        setUpMapIfNeeded();
        showPositionOnMap();
        plotMarker.setDraggable(true);
        SegmentedButton buttons = (SegmentedButton) findViewById(R.id.basemap_controls);
        MapHelper.setUpBasemapControls(buttons, mMap);
    }

    public void submitTreeMove(View view) {
        LatLng position = plotMarker.getPosition();
        try {
            Geometry g = plot.getGeometry();
            g.setY(position.latitude);
            g.setX(position.longitude);
            plot.setGeometry(g);
        } catch (JSONException e) {
            Logger.error(e);
        }
        Intent editPlot = new Intent(this, TreeEditDisplay.class);
        editPlot.putExtra("plot", plot.getData().toString());
        setResult(RESULT_OK, editPlot);
        finish();
    }
}
