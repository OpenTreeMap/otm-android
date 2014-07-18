package org.azavea.otm.fields;

import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class EcoField extends Field {
    private String currency;
    private String value;

    // Eco fields are calculated, not edited, so they have much less
    // information in their definition.
    public EcoField(JSONObject ecoFieldDef) {
        super(ecoFieldDef.optString("label"), ecoFieldDef.optString("label"));

        // Eco fields are described on a tree directly, so we have the value at
        // construction time.
        this.currency = ecoFieldDef.optString("currency_saved");
        this.value = ecoFieldDef.optString("value");
        if (!TextUtils.isEmpty(this.value)) {
            this.value += " " + ecoFieldDef.optString("unit");
        }
    }

    @Override
    public View renderForDisplay(LayoutInflater layout, Plot plot, Activity activity)
            throws JSONException {

        View container = layout.inflate(R.layout.plot_ecofield_row, null);
        ((TextView) container.findViewById(R.id.field_label)).setText(this.label);

        ((TextView) container.findViewById(R.id.field_value))
                .setText(this.value);

        ((TextView) container.findViewById(R.id.field_money))
                .setText(this.currency + " " +
                        activity.getString(R.string.eco_currencey_saved_text));
        return container;
    }

    @Override
    @Deprecated
    public View renderForEdit(LayoutInflater layout, Plot plot, Activity activity) {
        return null;
    }

    @Override
    @Deprecated
    protected Object getEditedValue() {
        return null;
    }
}
