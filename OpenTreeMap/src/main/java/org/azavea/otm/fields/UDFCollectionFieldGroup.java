package org.azavea.otm.fields;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Model;
import org.azavea.otm.data.Plot;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Lists.newArrayList;

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
    public View renderForDisplay(LayoutInflater inflater, Plot plot, Activity activity) {
        if (fieldKeys.length() == 0) {
            // If there are no fieldKeys, we shouldn't show the group at all
            return null;
        }
        View groupContainer = inflater.inflate(R.layout.plot_field_group, null);

        TextView groupLabel = (TextView) groupContainer.findViewById(R.id.group_name);
        groupLabel.setText(title);

        LinearLayout fieldContainer = (LinearLayout) groupContainer.findViewById(R.id.fields);
        fields = getFields(plot);
        if (fields.isEmpty()) {
            inflater.inflate(R.layout.collection_udf_empty, fieldContainer);
        } else {
            for (Field field : fields.values()) {
                View fieldView;
                try {
                    fieldView = field.renderForDisplay(inflater, plot, activity);
                } catch (JSONException e) {
                    fieldView = null;
                }
                if (fieldView != null) {
                    fieldContainer.addView(fieldView);
                }
            }
        }
        return groupContainer;
    }

    /**
     * Render a field group and its child fields for editing
     */
    @Override
    public View renderForEdit(LayoutInflater layout, Plot model, Activity activity) {
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
}
