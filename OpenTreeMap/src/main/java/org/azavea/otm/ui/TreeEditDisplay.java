package org.azavea.otm.ui;

import java.io.File;

import org.apache.http.Header;
import org.azavea.otm.App;
import org.azavea.otm.fields.FieldGroup;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.azavea.otm.fields.SpeciesField;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.LoggingJsonHttpResponseHandler;
import org.azavea.otm.rest.handlers.RestHandler;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

public class TreeEditDisplay extends TreeDisplay {
    // Intent Request codes
    public static final int FIELD_ACTIVITY_REQUEST_CODE = 0;
    protected static final int TREE_MOVE = 2;
    protected static final int PHOTO_USING_CAMERA_RESPONSE = 7;
    protected static final int PHOTO_USING_GALLERY_RESPONSE = 8;

    private ProgressDialog deleteDialog = null;
    private ProgressDialog saveDialog = null;

    private static String outputFilePath;

    private final RestHandler<Plot> deleteTreeHandler = new RestHandler<Plot>(new Plot()) {
        @Override
        public void failure(Throwable e, String message) {
            safeDismiss(deleteDialog);
            Toast.makeText(App.getAppInstance(), "Unable to delete tree", Toast.LENGTH_SHORT).show();
            Log.e(App.LOG_TAG, "Unable to delete tree.");
        }

        @Override
        public void dataReceived(Plot response) {
            safeDismiss(deleteDialog);
            Toast.makeText(App.getAppInstance(), "The tree was deleted.", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();

            // The tree was deleted, so return to the info page, and bring along
            // the data for the new plot, which was the response from the
            // delete operation
            resultIntent.putExtra("plot", response.getData().toString());
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    };

    private final JsonHttpResponseHandler deletePlotHandler = new LoggingJsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                if (response.getBoolean("ok")) {
                    safeDismiss(deleteDialog);
                    Toast.makeText(App.getAppInstance(), "The planting site was deleted.", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_PLOT_DELETED);
                    finish();

                }
            } catch (JSONException e) {
                safeDismiss(deleteDialog);
                Toast.makeText(App.getAppInstance(), "Unable to delete plot", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void failure(Throwable e, String message) {
            safeDismiss(deleteDialog);
            Toast.makeText(App.getAppInstance(), "Unable to delete plot", Toast.LENGTH_SHORT).show();
        }
    };

    private Bitmap newTreePhoto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mapFragmentId = R.id.vignette_map_edit_mode;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_edit_activity);
        setUpMapIfNeeded();
        initializeEditPage();
        mMap.setOnMapClickListener(point -> {
            Intent treeMoveIntent = new Intent(TreeEditDisplay.this, TreeMove.class);
            treeMoveIntent.putExtra("plot", plot.getData().toString());
            startActivityForResult(treeMoveIntent, TREE_MOVE);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        plotLocation = getPlotLocation(plot);
        showPositionOnMap();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tree_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        int id = item.getItemId();
        if (id == R.id.plot_save_button) {
            save();
            return true;
        } else if (id == R.id.edit_tree_picture) {
            changeTreePhoto();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeEditPage() {

        if (plot == null) {
            finish();
        }

        LinearLayout fieldList = (LinearLayout) findViewById(R.id.field_list);
        LayoutInflater layout = this.getLayoutInflater();

        // Add all the fields to the display for edit mode
        for (FieldGroup group : App.getFieldManager().getFieldGroups()) {
            View fieldGroup = group.renderForEdit(layout, plot, TreeEditDisplay.this, fieldList);
            if (fieldGroup != null) {
                fieldList.addView(fieldGroup);
            }
        }

        setupDeleteButtons(layout, fieldList);
    }

    /**
     * Delete options for tree and plot are available under certain situations
     * as reported from the /plot API endpoint as attributes of a plot/user
     * combo. Don't give delete tree option if a tree isn't present
     */
    private void setupDeleteButtons(LayoutInflater layout, LinearLayout fieldList) {
        View actionPanel = layout.inflate(R.layout.plot_edit_delete_buttons, null);
        actionPanel.findViewById(R.id.delete_plot).setVisibility(View.GONE);
        actionPanel.findViewById(R.id.delete_tree).setVisibility(View.GONE);
        fieldList.addView(actionPanel);

    }

    public void confirmDelete(int messageResource, final Callback callback) {
        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.confirm_delete)
                .setMessage(messageResource).setPositiveButton(R.string.delete, (dialog, which) -> {
            deleteDialog = ProgressDialog.show(thisActivity, "", "Deleting...", true);
            Message resultMessage = new Message();
            Bundle data = new Bundle();
            data.putBoolean("confirm", true);
            resultMessage.setData(data);
            callback.handleMessage(resultMessage);
        }).setNegativeButton(R.string.cancel, null).show();

    }

    public void deleteTree(View view) {
        Callback confirm = msg -> {
            if (msg.getData().getBoolean("confirm")) {

                RequestGenerator rc = new RequestGenerator();
                try {
                    rc.deleteCurrentTreeOnPlot(App.getAppInstance(), plot.getId(), deleteTreeHandler);
                } catch (JSONException e) {
                    Log.e(App.LOG_TAG, "Error deleting tree", e);
                }
            }
            return true;
        };

        confirmDelete(R.string.confirm_delete_tree_msg, confirm);
    }

    public void deletePlot(View view) {
        Callback confirm = msg -> {
            if (msg.getData().getBoolean("confirm")) {
                RequestGenerator rc = new RequestGenerator();
                try {
                    rc.deletePlot(App.getAppInstance(), plot.getId(), deletePlotHandler);
                } catch (JSONException e) {
                    Log.e(App.LOG_TAG, "Error deleting tree plot", e);
                }
            }
            return true;
        };

        confirmDelete(R.string.confirm_delete_plot_msg, confirm);
    }

    /**
     * By default cancel() will finish the activity
     */
    public void cancel() {
        cancel(true);
    }

    /**
     * Cancel the editing and return to the view profile, unchanged.
     *
     * @param doFinish - if the back button was pushed, finished will be called for
     *                 you
     */
    public void cancel(boolean doFinish) {
        setResult(RESULT_CANCELED);

        if (doFinish) {
            finish();
        }
    }

    private void save() {
        saveDialog = ProgressDialog.show(this, "", "Saving...", true);

        try {

            for (FieldGroup group : App.getFieldManager().getFieldGroups()) {
                group.update(plot);
            }

            RequestGenerator rg = new RequestGenerator();

            RestHandler<Plot> responseHandler = new RestHandler<Plot>(new Plot()) {
                @Override
                public void dataReceived(Plot updatedPlot) {
                    // Tree was updated, check if a photo needs to be also added
                    savePhotoForPlot(updatedPlot);
                }

                @Override
                public void failure(Throwable e, String responseBody) {
                    Log.e("REST", responseBody, e);
                    handleSaveFailure(e);
                }
            };

            if (addMode()) {
                rg.addPlot(plot, responseHandler);
            } else {
                if (App.getCurrentInstance().canEditTree()) {
                    rg.updatePlot(plot, responseHandler);
                } else {
                    savePhotoForPlot(plot);
                }
            }
        } catch (Exception e) {
            handleSaveFailure(e);
        }
    }

    private void safeDismiss(ProgressDialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void handleSaveFailure (Throwable e) {
        String msg = getString(R.string.save_tree_failure);
        Log.e(App.LOG_TAG, msg, e);
        safeDismiss(saveDialog);
        Toast.makeText(App.getAppInstance(), msg, Toast.LENGTH_SHORT).show();
    }

    private void handlePhotoSaveFailure (Throwable e) {
        String msg = getString(R.string.save_tree_photo_failure);
        Log.e(App.LOG_TAG, msg, e);
        safeDismiss(saveDialog);
        Toast.makeText(App.getAppInstance(), msg, Toast.LENGTH_SHORT).show();
    }

    private void savePhotoForPlot(final Plot updatedPlot) {
        if (this.newTreePhoto == null) {
            doFinish(updatedPlot, saveDialog);
            return;
        }

        if (!App.getCurrentInstance().canEditTreePhoto()) {
            handlePhotoSaveFailure(null);
            return;
        }

        RequestGenerator rc = new RequestGenerator();
        try {
            rc.addTreePhoto(updatedPlot, this.newTreePhoto, new LoggingJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if (response.has("image")) {
                            updatedPlot.assignNewTreePhoto(response);

                            safeDismiss(saveDialog);
                            doFinish(updatedPlot, saveDialog);

                        } else {
                            handlePhotoSaveFailure(null);
                            Log.d("AddTreePhoto", "photo response no success");
                        }
                    } catch (JSONException e) {
                        handlePhotoSaveFailure(e);
                    }
                }

                @Override
                public void failure(Throwable e, String errorResponse) {
                    handlePhotoSaveFailure(e);
                }
            });
        } catch (JSONException e) {
            handlePhotoSaveFailure(e);
        }
    }

    private void doFinish(Plot updatedPlot, ProgressDialog saveDialog) {
        safeDismiss(saveDialog);
        setResultOk(updatedPlot);

        // Updating may have changed the georev
        App.getCurrentInstance().setGeoRevId(plot.getUpdatedGeoRev());

        finish();
    }

    /**
     * Is the intent in add tree mode?
     */
    private boolean addMode() {
        return (getIntent().getStringExtra("new_tree") != null) && getIntent().getStringExtra("new_tree").equals("1");
    }

    /**
     * Set the result code to OK and set the updated plot as an intent extra
     *
     * @param updatedPlot
     */
    private void setResultOk(Plot updatedPlot) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("plot", updatedPlot.getData().toString());
        setResult(RESULT_OK, resultIntent);
    }

    @Override
    public void onBackPressed() {
        cancel(false);
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // In order to allow Fields to handle activity results themselves, share the activity
            // result with all of the FieldGroups, which will dispatch to the appropriate fields
            // based on the keys in the intent data
            case FIELD_ACTIVITY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    for (FieldGroup group : App.getFieldManager().getFieldGroups()) {
                        group.receiveActivityResult(resultCode, data, this);
                    }
                }
                break;
            case TREE_MOVE:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        plot.setData(new JSONObject(data.getStringExtra("plot")));
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    plotLocation = getPlotLocation(plot);
                    showPositionOnMap();
                }
                break;

            case PHOTO_USING_CAMERA_RESPONSE:
                if (resultCode == RESULT_OK) {
                    changePhotoUsingCamera(outputFilePath);
                }
                break;
            case PHOTO_USING_GALLERY_RESPONSE:
                if (resultCode == RESULT_OK) {
                    changePhotoUsingGallery(data);
                }
                break;
        }

    }

    /*
     * Photo Editing Functions
     */
    public void changeTreePhoto() {
        Log.d("PHOTO", "changePhoto");

        if (!App.getCurrentInstance().canEditTreePhoto()) {
            Toast.makeText(getApplicationContext(), getString(R.string.perms_add_tree_photo_fail), Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton(R.string.use_camera, (dialog, id) -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File outputFile = PhotoActivity.createImageFile();
            outputFilePath = outputFile.getAbsolutePath();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile));
            startActivityForResult(intent, PHOTO_USING_CAMERA_RESPONSE);
        });
        builder.setPositiveButton(R.string.use_gallery, (dialog, id) -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PHOTO_USING_GALLERY_RESPONSE);
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    // This function is called at the end of the whole camera process. You might
    // want to call your rc.submit method here, or store the bm in a class level
    // variable.
    protected void submitBitmap(Bitmap bm) {
        this.newTreePhoto = bm;
    }

    protected void changePhotoUsingCamera(String filePath) {
        Bitmap pic = PhotoActivity.getCorrectedCameraBitmap(filePath);
        if (pic != null) {
            submitBitmap(pic);
        }
    }

    protected void changePhotoUsingGallery(Intent data) {
        submitBitmap(PhotoActivity.getCorrectedGalleryBitmap(data));
    }

}
