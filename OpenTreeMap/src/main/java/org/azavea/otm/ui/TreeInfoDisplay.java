package org.azavea.otm.ui;

import org.azavea.otm.App;
import org.azavea.otm.fields.EcoField;
import org.azavea.otm.fields.FieldGroup;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.Tree;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.loopj.android.http.BinaryHttpResponseHandler;

public class TreeInfoDisplay extends TreeDisplay {
    public final static int EDIT_REQUEST = 1;
    ImageView plotImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mapFragmentId = R.id.vignette_map_view_mode;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_view_activity);
        findViewById(R.id.plot_edit_button).setOnClickListener(this::doEdit);
        setUpMapIfNeeded();
        plotImage = (ImageView) findViewById(R.id.plot_photo);
        loadPlotInfo();
    }

    private void loadPlotInfo() {

        try {
            LinearLayout fieldList = (LinearLayout) findViewById(R.id.field_list);
            fieldList.removeAllViewsInLayout();
            LayoutInflater layout = getLayoutInflater();

            setHeaderValues(plot);
            showPositionOnMap();
            for (FieldGroup group : App.getFieldManager().getFieldGroups()) {
                View fieldGroup = group.renderForDisplay(layout, plot, TreeInfoDisplay.this);
                if (fieldGroup != null) {
                    fieldList.addView(fieldGroup);
                }
            }

            // Eco benefit fields are not defined on the instance, but directly
            // on the plot. Create and render a field group on the fly
            View ecoFields = createEcoGroup(plot, layout);
            if (ecoFields != null) {
                fieldList.addView(ecoFields);
            }

            showImage(plot);
        } catch (Exception e) {
            Log.e(App.LOG_TAG, "Unable to render tree view", e);
            Toast.makeText(App.getAppInstance(), "Unable to render view for display", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private View createEcoGroup(Plot plot, LayoutInflater layout) {

        FieldGroup ecoGroup = new FieldGroup(getString(R.string.eco_fieldgroup_header));
        JSONObject benefits = (JSONObject) plot.getField("benefits");
        if (benefits == null) {
            return null;
        }
        JSONObject eco = benefits.optJSONObject("plot");
        if (eco == null) {
            return null;
        }
        String[] ecoKeys = App.getFieldManager().getEcoKeys();
        if (ecoKeys == null) {
            return null;
        }

        // Render eco fields based on instance eco field key order
        for (String key : ecoKeys) {
            JSONObject ecoField = eco.optJSONObject(key);
            if (ecoField != null) {
                ecoGroup.addField(new EcoField(ecoField));
            }
        }
        return ecoGroup.renderForDisplay(layout, plot, TreeInfoDisplay.this);

    }

    private void setHeaderValues(Plot plot) {
        try {

            String streetAddress = plot.getAddressStreet();
            if (!TextUtils.isEmpty(streetAddress)) {
                setText(R.id.address, streetAddress);
            }

            Tree tree = plot.getTree();
            String defaultText = getResources().getString(R.string.species_missing);
            if (tree != null) {
                setText(R.id.species, plot.getTitle());
            } else {
                setText(R.id.species, defaultText);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Could not access plot information for display", Toast.LENGTH_SHORT).show();
            Log.e(App.LOG_TAG, "Failed to create tree view", e);
        }

    }

    private void showImage(Plot plot) {
        // Default if there is no image returned
        plotImage.setImageResource(R.drawable.missing_tree_photo);

        plot.getTreeThumbnail(new BinaryHttpResponseHandler(Plot.IMAGE_TYPES) {
            @Override
            public void onSuccess(byte[] imageData) {
                plotImage.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
            }

            @Override
            public void onFailure(Throwable e, byte[] imageData) {
                // Log the error, but not important enough to bother the user
                Log.e(App.LOG_TAG, "Could not retreive tree image", e);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EDIT_REQUEST:

                if (resultCode == Activity.RESULT_OK) {
                    // The tree/plot has been updated, or the tree has been deleted
                    String plotJSON = data.getStringExtra("plot");
                    if (plotJSON != null) {

                        try {
                            // The plot has been edited, reload the info page
                            plot = new Plot(new JSONObject(plotJSON));
                            loadPlotInfo();

                            plotLocation = getPlotLocation(plot);
                            showPositionOnMap();

                            // Pass along the updated plot
                            Intent updatedPlot = new Intent();
                            updatedPlot.putExtra("plot", plotJSON);
                            setResult(TreeDisplay.RESULT_PLOT_EDITED, updatedPlot);

                        } catch (JSONException e) {
                            Log.e(App.LOG_TAG, "Unable to load edited plot");
                            finish();
                        }
                    }

                } else if (resultCode == RESULT_PLOT_DELETED) {
                    // If the plot is deleted, finish back to the caller
                    setResult(RESULT_PLOT_DELETED);
                    finish();

                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Do nothing?
                }
                break;

        }
    }

    public void doEdit(View view) {
        if (!App.getLoginManager().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if (!App.getCurrentInstance().canEditTree()) {
            Toast.makeText(getApplicationContext(), getString(R.string.perms_edit_tree_fail), Toast.LENGTH_SHORT).show();
        } else {
            // TODO: is this try block necessary?
            try {
                Tree tree = plot.getTree();
            } catch (JSONException e) {
                Log.e(App.LOG_TAG, "Could not check tree details", e);
                Toast.makeText(getApplicationContext(), "Could not check tree details", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent editPlot = new Intent(this, TreeEditDisplay.class);
            editPlot.putExtra("plot", plot.getData().toString());
            startActivityForResult(editPlot, EDIT_REQUEST);
        }
    }

    public void handlePhotoDetailClick(View view) {
        plot.getTreePhoto(MapHelper.getPhotoDetailHandler(this));
    }
}
