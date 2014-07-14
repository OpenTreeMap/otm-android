package org.azavea.otm.fields;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.azavea.otm.R;
import org.azavea.otm.data.Model;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.Species;
import org.json.JSONException;
import org.json.JSONObject;

public class SpeciesField extends ButtonField {

    SpeciesField(JSONObject fieldDef) {
        super(fieldDef);
    }

    /*
     * Render a view to display the given model field in view mode
     */
    @Override
    public View renderForDisplay(LayoutInflater layout, Plot model, Context context) throws JSONException {
        View container = layout.inflate(R.layout.plot_field_row, null);
        TextView label = (TextView) container.findViewById(R.id.field_label);
        TextView fieldValue = (TextView) container.findViewById(R.id.field_value);

        // tree.species gets exploded to a double row with sci name and common name
        label.setText("Scientific Name");
        fieldValue.setText(formatUnit(model.getScienticName()));

        // TODO: It would be much better if this LinearLayout was defined in XML
        LinearLayout doubleRow = new LinearLayout(context);
        doubleRow.setOrientation(LinearLayout.VERTICAL);
        doubleRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        View containerCommon = layout.inflate(R.layout.plot_field_row, null);
        TextView labelCommon = (TextView) containerCommon.findViewById(R.id.field_label);
        TextView fieldValueCommon = (TextView) containerCommon.findViewById(R.id.field_value);

        labelCommon.setText("Common Name");
        fieldValueCommon.setText(formatUnit(model.getCommonName()));

        doubleRow.addView(container);
        doubleRow.addView(containerCommon);

        return doubleRow;
    }

    @Override
    protected void setupButton(Button button, Object value, Model model) {
        JSONObject json = model.getData();

        // species could either be truly null, or an actual but empty JSONObject {}
        if (!JSONObject.NULL.equals(value)) {
            // Set the button text to the common and sci name
            String sciName = (String) getValueForKey("tree.species.scientific_name", json);
            String commonName = (String) getValueForKey("tree.species.common_name", json);
            button.setText(commonName + "\n" + sciName);
            Species speciesValue = new Species();
            speciesValue.setData((JSONObject) value);
            this.setValue(speciesValue);
        } else {
            button.setText(R.string.unspecified_field_value);
        }
    }

    public void attachClickListener(View.OnClickListener speciesClickListener) {
        if (this.valueView != null) {
            this.valueView.setOnClickListener(speciesClickListener);
        }
    }

    /**
     * Manual setting of the field value from an external client. The only
     * current use case for this is setting the species value on a species
     * selector from the calling activity.
     */
    public void setValue(Object value) {
        if (key.equals(TREE_SPECIES)) {
            Species species = (Species) value;

            if (this.valueView != null) {

                Button speciesButton = (Button) this.valueView;

                if (species != null) {

                    speciesButton.setTag(R.id.choice_button_value_tag, species.getData());
                    String label = species.getCommonName() + "\n" + species.getScientificName();
                    speciesButton.setText(label);
                }
            }
        }
    }
}