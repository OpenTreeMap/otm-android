package org.azavea.otm.fields;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.azavea.otm.ui.TreeEditDisplay;
import org.azavea.otm.ui.UDFCollectionCreateActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.partition;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Collections2.transform;

/**
 * UDF Collections are odd and don't really fit into the ecosystem of other fields.
 * <p>
 * A UDF collection has one or more keys, and each entry on a Model has a list of zero or more
 * entries per UDF collection field
 * <p>
 * Each UDFCollectionFieldGroup can have multiple UDF collection fields that are shown in that group
 * All collection entries are merged together by a sort key, which *must* exist on every collection
 * UDF in the group.
 * <p>
 * It isn't known until we pull the values from the Plot and Tree models how many rows we
 * will need to display.
 * As such, it doesn't make it's Field(s) until it is rendered, and the number of Fields may change
 */
public class UDFCollectionFieldGroup extends FieldGroup {

    private static final int NUM_FIELDS_PER_CLICK = 3;

    private String title;
    private String sortKey;
    private ArrayList<JSONObject> udfDefinitions = new ArrayList<>();
    private List<UDFCollectionValueField> fields;

    private final JSONArray fieldKeys;

    public UDFCollectionFieldGroup(JSONObject groupDefinition,
                                   Map<String, JSONObject> fieldDefinitions) throws JSONException {

        title = groupDefinition.optString("header");
        sortKey = groupDefinition.optString("sort_key");
        fieldKeys = groupDefinition.getJSONArray("collection_udf_keys");
        for (int i = 0; i < fieldKeys.length(); i++) {
            String key = fieldKeys.getString(i);
            udfDefinitions.add(fieldDefinitions.get(key));
        }
    }

    /**
     * Render a field group and its child fields for viewing
     */
    @Override
    public View renderForDisplay(LayoutInflater inflater, Plot plot, Activity activity, ViewGroup parent) {
        return render(inflater, plot, activity, parent, DisplayMode.VIEW);
    }

    /**
     * Render a field group and its child fields for editing
     */
    @Override
    public View renderForEdit(LayoutInflater inflater, Plot plot, Activity activity, ViewGroup parent) {
        return render(inflater, plot, activity, parent, DisplayMode.EDIT);
    }

    @Override
    public void update(Plot plot) {
        // TODO: Implement
    }

    /**
     * Handles the common functionality between rendering collection UDFs for edit and for display
     * Dispatches to helpers for those parts which are different based on DisplayMode
     */
    private View render(LayoutInflater inflater, Plot plot, Activity activity, ViewGroup parent, DisplayMode mode) {
        if (fieldKeys.length() == 0) {
            // If there are no fieldKeys, we shouldn't show the group at all
            return null;
        }
        final View groupContainer = inflater.inflate(R.layout.collection_udf_field_group, parent, false);

        final TextView groupLabel = (TextView) groupContainer.findViewById(R.id.group_name);
        groupLabel.setText(title);

        final LinearLayout fieldContainer = (LinearLayout) groupContainer.findViewById(R.id.fields);
        fields = getFields(plot);

        final View buttonContainer = groupContainer.findViewById(R.id.udf_button_container);
        final Button button = (Button) groupContainer.findViewById(R.id.udf_button);

        final Collection<View> fieldViews = getViewsForFields(inflater, plot, activity, fieldContainer);

        if (mode == DisplayMode.VIEW) {
            setupFieldsForDisplay(inflater, fieldContainer, buttonContainer, button, fieldViews);
        } else {
            setupFieldsForEdit(fieldContainer, button, fieldViews, activity);
        }

        return groupContainer;
    }

    private void setupFieldsForDisplay(LayoutInflater inflater, LinearLayout fieldContainer,
                                       View buttonContainer, Button button, Collection<View> fieldViews) {
        button.setText(R.string.load_more_collection_udf);

        if (fieldViews.isEmpty()) {
            inflater.inflate(R.layout.collection_udf_empty, fieldContainer);
            buttonContainer.setVisibility(View.GONE);
        } else {
            // We only want to show so many fields at a time, and add more when a "Show More" button is clicked
            final List<List<View>> fieldViewGroups = newArrayList(partition(fieldViews, NUM_FIELDS_PER_CLICK));

            addFieldsToGroup(fieldViewGroups.remove(0), fieldContainer);

            if (fieldViewGroups.isEmpty()) {
                buttonContainer.setVisibility(View.GONE);
            } else {
                button.setOnClickListener(v -> {
                    addFieldsToGroup(fieldViewGroups.remove(0), fieldContainer);
                    if (fieldViewGroups.isEmpty()) {
                        buttonContainer.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    private void setupFieldsForEdit(LinearLayout fieldContainer, Button button, Collection<View> fieldViews, Activity activity) {
        // TODO: String extraction
        button.setText("Add New");

        for (View view : fieldViews) {
            fieldContainer.addView(view);
        }

        button.setOnClickListener(v -> {
            Intent udfCreator = new Intent(App.getAppInstance(), UDFCollectionCreateActivity.class);

            // JSONObject is not serializable, so we send the string representation
            Collection<String> jsonUdfDefs = transform(udfDefinitions, JSONObject::toString);
            udfCreator.putExtra(UDFCollectionCreateActivity.UDF_DEFINITIONS, newArrayList(jsonUdfDefs));

            activity.startActivityForResult(udfCreator, TreeEditDisplay.FIELD_ACTIVITY_REQUEST_CODE);
        });
    }

    private List<UDFCollectionValueField> getFields(Plot plot) {
        List<UDFCollectionValueField> fieldsList = newArrayList();
        for (JSONObject udfDef : udfDefinitions) {
            JSONArray collectionValues = (JSONArray) plot.getValueForKey(udfDef.optString("field_key"));
            for (int i = 0; i < collectionValues.length(); i++) {
                JSONObject value = collectionValues.optJSONObject(i);
                fieldsList.add(new UDFCollectionValueField(i, udfDef, sortKey, value));
            }
        }
        Collections.sort(fieldsList);

        return fieldsList;
    }

    private Collection<View> getViewsForFields(LayoutInflater inflater, Plot plot, Activity activity, LinearLayout fieldContainer) {
        return filter(transform(fields, field -> {
            try {
                return field.renderForDisplay(inflater, plot, activity, fieldContainer);
            } catch (JSONException e) {
                return null;
            }
        }), view -> view != null);
    }

    private void addFieldsToGroup(List<View> fieldGroup, ViewGroup parent) {
        for (View fieldView : fieldGroup) {
            parent.addView(fieldView);
        }
    }
}
