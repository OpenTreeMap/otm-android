package org.azavea.otm.fields;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class DiameterField extends TextField {

    protected DiameterField(JSONObject fieldDef) {
        super(fieldDef);
    }

    /*
     * Render a view to display the given model field in edit mode
     */
    @Override
    public View renderForEdit(LayoutInflater inflater, Plot model, Activity activity) {
        if (this.canEdit) {
            Object value = getValueForKey(this.key, model.getData());
            View container = inflater.inflate(R.layout.plot_field_edit_diameter_row, null);

            View diameterRow = container.findViewById(R.id.diameter_row);

            ((TextView) diameterRow.findViewById(R.id.field_label)).setText(this.label);
            EditText diameterEdit = (EditText) diameterRow.findViewById(R.id.field_value);
            TextView diameterUnitLabel = ((TextView) diameterRow.findViewById(R.id.field_unit));

            String safeValue = (!JSONObject.NULL.equals(value)) ? value.toString() : "";
            diameterEdit.setText(safeValue);
            this.valueView = diameterEdit;
            diameterUnitLabel.setText(this.unitText);
            setFieldKeyboard(diameterEdit);

            View circRow = container.findViewById(R.id.circumference_row);
            ((TextView) circRow.findViewById(R.id.field_label)).setText(R.string.circumference_label);
            EditText circumferenceEdit = (EditText) circRow.findViewById(R.id.field_value);
            setFieldKeyboard(circumferenceEdit);
            ((TextView) circRow.findViewById(R.id.field_unit)).setText(this.unitText);

            setupCircumferenceField(diameterEdit, circumferenceEdit);

            return container;
        }
        return null;
    }



    /**
     * If the tree.diameter field exists for editing, also include a synced
     * circumference field so that the user can enter either measurement
     */
    private void setupCircumferenceField(final EditText diameter, final EditText circumference) {
        diameter.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                handleTextChange(diameter, circumference, false);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        circumference.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                handleTextChange(circumference, diameter, true);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        diameter.setText(diameter.getText());
    }

    private void handleTextChange(EditText editing, EditText receiving, boolean calcDbh) {
        try {
            DecimalFormat df = new DecimalFormat("#.##");

            String editingText = editing.getText().toString();
            String other = receiving.getText().toString();
            double otherVal = (other.equals("") || other.equals(".")) ? 0 : Double.parseDouble(other);

            // If the value was blanked, and the other is not already blank,
            // blank it
            if (editingText.equals("")) {
                if (other.equals("")) {
                    return;
                } else {
                    receiving.setText("");
                }
                return;
            }

            String display;
            double calculatedVal;

            // Handle cases where the first input is a decimal point
            if (editingText.equals(".")) {
                editingText = "0.";
            }

            if (calcDbh) {
                double c = Double.parseDouble(editingText);
                calculatedVal = c / Math.PI;
            } else {
                double d = Double.parseDouble(editingText);
                calculatedVal = Math.PI * d;

            }
            display = df.format(calculatedVal);

            // Only set the other text if there is a significant difference
            // in from the calculated value
            if (Math.abs(otherVal - calculatedVal) >= 0.05) {
                receiving.setText(display);
                receiving.setSelection(display.length());
            }

        } catch (Exception e) {
            editing.setText("");
        }
    }
}