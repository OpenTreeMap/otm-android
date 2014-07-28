package org.azavea.otm.fields;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;

public class UDFCollectionValueField extends Field implements Comparable<UDFCollectionValueField> {
    private static final int DEFAULT_DIGITS = 2;

    private final JSONObject udfDefinition;
    private final Map<String, JSONObject> nameToType = new LinkedHashMap<>();
    private final String sortKey;
    private final JSONObject value;

    public UDFCollectionValueField(String collectionKey, int index, JSONObject udfDefinition,
                                   String sortKey, JSONObject value) {
        super(String.format("%s[%d]", collectionKey, index), getLabel(collectionKey));
        this.udfDefinition  = udfDefinition;
        this.sortKey = sortKey;
        this.value = value;

        // It is easier to work with the data by name, so we pull it into a LinkedHashMap
        JSONArray dataTypes = udfDefinition.optJSONArray("data_type");
        for (int i = 0; i < dataTypes.length(); i++) {
            JSONObject dataType = dataTypes.optJSONObject(i);
            nameToType.put(dataType.optString("name"), dataType);
        }
    }

    @Override
    public View renderForDisplay(LayoutInflater layout, Plot plot, Activity activity)
            throws JSONException {
        View container = layout.inflate(R.layout.collection_udf_element_row, null);
        TextView labelView = (TextView) container.findViewById(R.id.primary_text);
        TextView secondaryTextView = (TextView) container.findViewById(R.id.secondary_text);
        TextView sortTextView = (TextView) container.findViewById(R.id.sort_key_field);

        labelView.setText(label);

        List<String> secondaryText = new ArrayList<>();
        for (String key : nameToType.keySet()) {
            String formattedValue = formatSubValue(key);
            if (sortKey.equals(key)) {
                sortTextView.setText(formattedValue);
            } else {
                secondaryText.add(formattedValue);
            }
        }
        secondaryTextView.setText(Joiner.on('\n').join(secondaryText));

        return container;
    }

    @Override
    public View renderForEdit(LayoutInflater layout, Plot plot, Activity activity) {
        // TODO: Implement
        return null;
    }

    @Override
    protected Object getEditedValue() {
        // TODO: Implement
        return null;
    }

    private String formatSubValue(String key) {
        Object subValue = value.opt(key);
        JSONObject typeDef = nameToType.get(key);
        String type = typeDef.optString("type");

        if (JSONObject.NULL.equals(subValue)) {
            return App.getAppInstance().getString(R.string.unspecified_field_value);
        } else if ("date".equals(type)) {
            return DateField.formatTimestampForDisplay((String) subValue);
        } else if ("float".equals(type)) {
            return TextField.formatWithDigits(subValue, DEFAULT_DIGITS);
        }

        return String.valueOf(value.opt(key));
    }

    private static String getLabel(String collectionKey) {
        App app = App.getAppInstance();
        if (collectionKey.contains(".") && "tree".equals(collectionKey.split("[.]")[0])) {
            return app.getString(R.string.tree);
        } else {
            return app.getString(R.string.planting_site);
        }
    }

    @Override
    public int compareTo(UDFCollectionValueField another) {
        String sortKeyType = nameToType.get(sortKey).optString("type");

        if ("date".equals(sortKeyType)) {
            // Dates are serialized in ISO format, which allows us to sort them lexicographically
            return nullToEmpty(another.value.optString(sortKey)).compareTo(nullToEmpty(value.optString(sortKey)));
        } else if ("choice".equals(sortKey) || "string".equals(sortKey)) {
            // Natural String sorting
            return ComparisonChain.start()
                    .compare(this, another,
                            Ordering.natural()
                                    .nullsFirst()
                                    .reverse()
                                    .onResultOf(v -> v.value.optString(sortKey)))
                    .result();
        } else {
            return Doubles.compare(another.value.optDouble(sortKey, Double.MIN_VALUE), value.optDouble(sortKey, Double.MIN_VALUE));
        }
    }
}
