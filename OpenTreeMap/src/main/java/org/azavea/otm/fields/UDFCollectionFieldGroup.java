package org.azavea.otm.fields;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.azavea.helpers.JSONHelper;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.adapters.LinkedHashMapAdapter;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.UDFCollectionDefinition;
import org.azavea.otm.ui.TreeEditDisplay;
import org.azavea.otm.ui.UDFCollectionCreateActivity;
import org.azavea.otm.ui.UDFCollectionEditActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.getFirst;
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

    private final String title;
    private final String sortKey;
    private final LinkedHashMap<String, UDFCollectionDefinition> udfDefinitions = new LinkedHashMap<>();
    private final LinkedHashMap<String, UDFCollectionDefinition> editableUdfDefinitions = new LinkedHashMap<>();
    private final List<String> fieldKeys;

    private ViewGroup fieldContainer;
    private List<UDFCollectionValueField> fields;

    public UDFCollectionFieldGroup(JSONObject groupDefinition,
                                   Map<String, JSONObject> fieldDefinitions) throws JSONException {

        title = groupDefinition.optString("header");
        sortKey = groupDefinition.optString("sort_key");
        fieldKeys = JSONHelper.jsonStringArrayToList(groupDefinition.getJSONArray("collection_udf_keys"));

        for (String key : fieldKeys) {
            if (fieldDefinitions.containsKey(key)) {
                final UDFCollectionDefinition udfDef = new UDFCollectionDefinition(fieldDefinitions.get(key));
                udfDefinitions.put(key, udfDef);
                if (udfDef.isWritable()) {
                    editableUdfDefinitions.put(key, udfDef);
                }
            }
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
    public void receiveActivityResult(int resultCode, Intent data, Activity activity) {
        for (String key : editableUdfDefinitions.keySet()) {
            if (data.getExtras().containsKey(key)) {
                final String json = data.getStringExtra(key);
                final UDFCollectionDefinition udfDef = editableUdfDefinitions.get(key);
                final JSONObject value;
                try {
                    value = new JSONObject(json);
                } catch (JSONException e) {
                    Log.e(App.LOG_TAG, "Error parsing JSON passed as activity result", e);
                    continue;
                }
                final UDFCollectionValueField field = new UDFCollectionValueField(udfDef, sortKey, value);
                if (data.getExtras().containsKey(UDFCollectionEditActivity.TAG)) {
                    final int tag = data.getIntExtra(UDFCollectionEditActivity.TAG, -1);
                    if (tag == -1 || fields.isEmpty()) {
                        Log.w(App.LOG_TAG, "Invalid tag for UDF, ignoring it");
                    } else {
                        // If we received a modified field, remove the old field and add it back with new data
                        fields = newArrayList(concat(filter(fields, f -> f.getTag() != tag), newArrayList(field)));
                    }
                } else {
                    fields.add(field);
                }
            }
        }
        replaceEditFields(activity);
    }

    @Override
    public void update(Plot plot) {
        Map<String, JSONArray> collectionUdfArrays = new HashMap<>(fieldKeys.size());
        for (String collectionKey : fieldKeys) {
            collectionUdfArrays.put(collectionKey, new JSONArray());
        }
        for (Field field : fields) {
            String collectionKey = field.key;
            if (collectionUdfArrays.containsKey(collectionKey)) {
                JSONArray udfData = collectionUdfArrays.get(collectionKey);
                udfData.put(field.getEditedValue());
            } else {
                Log.w(App.LOG_TAG, "Impossible state - UDFCollectionGroup has a field not in it's fieldKeys");
            }
        }
        for (Map.Entry<String, JSONArray> entry : collectionUdfArrays.entrySet()) {
            try {
                plot.setValueForKey(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                // TODO: Extract String
                Toast.makeText(App.getAppInstance(), "Error saving Stewardship fields", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Handles the common functionality between rendering collection UDFs for edit and for display
     * Dispatches to helpers for those parts which are different based on DisplayMode
     */
    private View render(LayoutInflater inflater, Plot plot, Activity activity, ViewGroup parent, DisplayMode mode) {
        if (getCurrentUdfDefinitions(mode).isEmpty()) {
            // If there are no udfDefinitions, we shouldn't show the group at all
            return null;
        }
        final View groupContainer = inflater.inflate(R.layout.collection_udf_field_group, parent, false);

        final TextView groupLabel = (TextView) groupContainer.findViewById(R.id.group_name);
        groupLabel.setText(title);

        final View buttonContainer = groupContainer.findViewById(R.id.udf_button_container);
        final Button button = (Button) groupContainer.findViewById(R.id.udf_button);

        fields = getFields(plot, mode);
        fieldContainer = (LinearLayout) groupContainer.findViewById(R.id.fields);
        final Collection<View> fieldViews = getViewsForFields(inflater, activity, mode);

        if (mode == DisplayMode.VIEW) {
            setupFieldsForDisplay(inflater, buttonContainer, button, fieldViews);
        } else {
            setupFieldsForEdit(button, fieldViews, activity);
        }

        return groupContainer;
    }

    private void setupFieldsForDisplay(LayoutInflater inflater, View buttonContainer, Button button,
                                       Collection<View> fieldViews) {
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

    private void setupFieldsForEdit(Button button, Collection<View> fieldViews, Activity activity) {
        // TODO: String extraction
        button.setText("Add New");

        for (View view : fieldViews) {
            fieldContainer.addView(view);
        }

        button.setOnClickListener(v -> {
            Intent udfCreator = new Intent(App.getAppInstance(), UDFCollectionCreateActivity.class);

            udfCreator.putParcelableArrayListExtra(UDFCollectionCreateActivity.UDF_DEFINITIONS,
                    newArrayList(editableUdfDefinitions.values()));

            activity.startActivityForResult(udfCreator, TreeEditDisplay.FIELD_ACTIVITY_REQUEST_CODE);
        });
    }

    private void replaceEditFields(Activity activity) {
        fieldContainer.removeAllViews();
        Collections.sort(fields);

        LayoutInflater inflater = activity.getLayoutInflater();
        Collection<View> fieldViews = getViewsForFields(inflater, activity, DisplayMode.EDIT);
        for (View view : fieldViews) {
            fieldContainer.addView(view);
        }
    }

    private List<UDFCollectionValueField> getFields(Plot plot, DisplayMode mode) {
        final List<UDFCollectionValueField> fieldsList = newArrayList();
        for (UDFCollectionDefinition udfDef : getCurrentUdfDefinitions(mode).values()) {
            JSONArray collectionValues = (JSONArray) plot.getValueForKey(udfDef.getCollectionKey());
            if (!JSONObject.NULL.equals(collectionValues)) {
                for (int i = 0; i < collectionValues.length(); i++) {
                    JSONObject value = collectionValues.optJSONObject(i);
                    fieldsList.add(new UDFCollectionValueField(udfDef, sortKey, value));
                }
            }
        }

        return fieldsList;
    }

    private Map<String, UDFCollectionDefinition> getCurrentUdfDefinitions(DisplayMode mode) {
        return (mode == DisplayMode.VIEW) ? udfDefinitions : editableUdfDefinitions;
    }

    private Collection<View> getViewsForFields(LayoutInflater inflater, Activity activity, DisplayMode mode) {
        Collections.sort(fields);
        return filter(transform(fields, field -> {
            try {
                if (mode == DisplayMode.VIEW) {
                    return field.renderForDisplay(inflater, activity, fieldContainer);
                } else {
                    return field.renderForEdit(inflater, activity, fieldContainer);
                }
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
