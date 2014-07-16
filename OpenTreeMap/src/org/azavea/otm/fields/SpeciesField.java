package org.azavea.otm.fields;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Model;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.Species;
import org.azavea.otm.ui.SpeciesListDisplay;
import org.azavea.otm.ui.TreeEditDisplay;
import org.json.JSONException;
import org.json.JSONObject;

public class SpeciesField extends ButtonField {

    protected SpeciesField(JSONObject fieldDef) {
        super(fieldDef);
    }

    /*
     * Render a view to display the given model field in view mode
     */
    @Override
    public View renderForDisplay(LayoutInflater layout, Plot model, Activity activity) throws JSONException {
        View container = layout.inflate(R.layout.plot_field_row, null);
        TextView label = (TextView) container.findViewById(R.id.field_label);
        TextView fieldValue = (TextView) container.findViewById(R.id.field_value);

        // tree.species gets exploded to a double row with sci name and common name
        label.setText("Scientific Name");
        fieldValue.setText(formatValue(model.getScienticName()));

        // TODO: It would be much better if this LinearLayout was defined in XML
        LinearLayout doubleRow = new LinearLayout(activity);
        doubleRow.setOrientation(LinearLayout.VERTICAL);
        doubleRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        View containerCommon = layout.inflate(R.layout.plot_field_row, null);
        TextView labelCommon = (TextView) containerCommon.findViewById(R.id.field_label);
        TextView fieldValueCommon = (TextView) containerCommon.findViewById(R.id.field_value);

        labelCommon.setText("Common Name");
        fieldValueCommon.setText(formatValue(model.getCommonName()));

        doubleRow.addView(container);
        doubleRow.addView(containerCommon);

        return doubleRow;
    }

    @Override
    protected void setupButton(Button button, Object value, Model model, Activity activity) {
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
        button.setOnClickListener(v -> {
            Intent speciesSelector = new Intent(App.getAppInstance(), SpeciesListDisplay.class);
            activity.startActivityForResult(speciesSelector, TreeEditDisplay.FIELD_ACTIVITY_REQUEST_CODE);
        });
    }

    @Override
    public void receiveActivityResult(int resultCode, Intent data) {
        CharSequence speciesJSON = data.getCharSequenceExtra(Field.TREE_SPECIES);
        if (!JSONObject.NULL.equals(speciesJSON)) {
            Species species = new Species();
            try {

                species.setData(new JSONObject(speciesJSON.toString()));
                setValue(species);

            } catch (JSONException e) {
                String msg = "Unable to retrieve selected species";
                Log.e(App.LOG_TAG, msg, e);
                Toast.makeText(App.getAppInstance(), msg, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Manual setting of the field value from an external client. The only
     * current use case for this is setting the species value on a species
     * selector from the calling activity.
     */
    public void setValue(Species species) {
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