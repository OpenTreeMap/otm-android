package org.azavea.otm.fields;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.Iterables;

import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private JSONArray fieldKeys;
    private Map<String, JSONObject> udfDefinitions = new LinkedHashMap<>();

    public UDFCollectionFieldGroup(JSONObject groupDefinition,
                                   Map<String, JSONObject> fieldDefinitions) throws JSONException {

        title = groupDefinition.optString("header");
        sortKey = groupDefinition.optString("sort_key");
        fieldKeys = groupDefinition.getJSONArray("collection_udf_keys");
        for (int i = 0; i < fieldKeys.length(); i++) {
            String key = fieldKeys.getString(i);
            udfDefinitions.put(key, fieldDefinitions.get(key));
        }
    }

    /**
     * Render a field group and its child fields for viewing
     */
    @Override
    public View renderForDisplay(LayoutInflater inflater, Plot plot, Activity activity, ViewGroup parent) {
        if (fieldKeys.length() == 0) {
            // If there are no fieldKeys, we shouldn't show the group at all
            return null;
        }
        final View groupContainer = inflater.inflate(R.layout.collection_udf_field_group, parent, false);

        final TextView groupLabel = (TextView) groupContainer.findViewById(R.id.group_name);
        groupLabel.setText(title);

        final LinearLayout fieldContainer = (LinearLayout) groupContainer.findViewById(R.id.fields);
        fields = getFields(plot);

        final View showMore = groupContainer.findViewById(R.id.show_more_button_container);
        final Button showMoreButton = (Button) groupContainer.findViewById(R.id.show_more_button);

        final Collection<View> fieldViews = filter(transform(fields.values(), field -> {
            try {
                return field.renderForDisplay(inflater, plot, activity, fieldContainer);
            } catch (JSONException e) {
                return null;
            }
        }), view -> view != null);

        if (fieldViews.isEmpty()) {
            inflater.inflate(R.layout.collection_udf_empty, fieldContainer);
            showMore.setVisibility(View.GONE);
        } else {
            // We only want to show so many fields at a time, and add more when a "Show More" button is clicked
            final List<List<View>> fieldViewGroups = newArrayList(partition(fieldViews, NUM_FIELDS_PER_CLICK));

            addFieldsToGroup(fieldViewGroups.remove(0), fieldContainer);

            if (fieldViewGroups.isEmpty()) {
                showMore.setVisibility(View.GONE);
            } else {
                showMoreButton.setOnClickListener(v -> {
                    addFieldsToGroup(fieldViewGroups.remove(0), fieldContainer);
                    if (fieldViewGroups.isEmpty()) {
                        showMore.setVisibility(View.GONE);
                    }
                });
            }
        }
        return groupContainer;
    }

    /**
     * Render a field group and its child fields for editing
     */
    @Override
    public View renderForEdit(LayoutInflater layout, Plot model, Activity activity, ViewGroup parent) {
        // TODO: Implement edit mode
        return null;
    }

    @Override
    public void update(Model model) {
        // TODO: Implement
    }

    private Map<String, Field> getFields(Plot plot) {
        List<UDFCollectionValueField> fieldsList = newArrayList();
        for (String key : udfDefinitions.keySet()) {
            JSONObject udfDef = udfDefinitions.get(key);
            JSONArray collectionValues = (JSONArray) plot.getValueForKey(key);
            for (int i = 0; i < collectionValues.length(); i++) {
                JSONObject value = collectionValues.optJSONObject(i);
                fieldsList.add(new UDFCollectionValueField(key, i, udfDef, sortKey, value));
            }
        }
        Collections.sort(fieldsList);

        LinkedHashMap<String, Field> fieldsMap = new LinkedHashMap<>(fieldsList.size());
        for (Field field : fieldsList) {
            fieldsMap.put(field.key, field);
        }

        return fieldsMap;
    }

    private void addFieldsToGroup(List<View> fieldGroup, ViewGroup parent) {
        for (View fieldView : fieldGroup) {
            parent.addView(fieldView);
        }
    }
}
