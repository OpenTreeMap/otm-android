package org.azavea.otm.fields;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;

import org.azavea.helpers.UDFCollectionHelper;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.azavea.otm.fields.FieldGroup.DisplayMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;

public class UDFCollectionValueField extends Field implements Comparable<UDFCollectionValueField> {
    private static final int DEFAULT_DIGITS = 2;

    private final HashMap<String, JSONObject> nameToType;
    private final String sortKey;
    private final JSONObject value;

    public UDFCollectionValueField(JSONObject udfDefinition, String sortKey, JSONObject value) {
        super(udfDefinition.optString("field_key"), UDFCollectionHelper.getLabel(udfDefinition));
        this.sortKey = sortKey;
        this.value = value;

        nameToType = UDFCollectionHelper.groupTypesByName(udfDefinition);
    }

    @Override
    public View renderForDisplay(LayoutInflater inflater, Plot plot, Activity activity, ViewGroup parent)
            throws JSONException {
        return render(inflater, plot, activity, parent, DisplayMode.VIEW);
    }

    @Override
    public View renderForEdit(LayoutInflater inflater, Plot plot, Activity activity, ViewGroup parent) {
        return render(inflater, plot, activity, parent, DisplayMode.EDIT);
    }

    private View render(LayoutInflater inflater, Plot plot, Activity activity, ViewGroup parent, DisplayMode mode) {
        View container = inflater.inflate(R.layout.collection_udf_element_row, parent, false);
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

        View chevron = container.findViewById(R.id.chevron);
        if (mode == DisplayMode.VIEW) {
            chevron.setVisibility(View.GONE);
        } else {
            chevron.setVisibility(View.VISIBLE);
            container.setOnClickListener(v -> {
                // TODO: Implement
            });
        }

        return container;
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
                                    .onResultOf(v -> v.value.optString(sortKey))
                    )
                    .result();
        } else {
            return Doubles.compare(another.value.optDouble(sortKey, Double.MIN_VALUE), value.optDouble(sortKey, Double.MIN_VALUE));
        }
    }
}
