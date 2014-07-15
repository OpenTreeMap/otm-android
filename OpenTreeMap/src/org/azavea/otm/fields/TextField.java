package org.azavea.otm.fields;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.json.JSONObject;

public class TextField extends Field {
    /**
     * The text to append to the value as a unit
     */
    public String unitText;

    /**
     * Number of significant digits to round to
     */
    public int digits;

    protected TextField(JSONObject fieldDef) {
        super(fieldDef);

        unitText = fieldDef.optString("units");
        digits = fieldDef.optInt("digits");
    }

    /*
     * Render a view to display the given model field in edit mode
     */
    @Override
    public View renderForEdit(LayoutInflater layout, Plot model, Context context) {

        View container = null;

        if (this.canEdit) {
            container = layout.inflate(R.layout.plot_field_edit_row, null);
            Object value = getValueForKey(this.key, model.getData());

            ((TextView) container.findViewById(R.id.field_label)).setText(this.label);
            EditText edit = (EditText) container.findViewById(R.id.field_value);
            Button choiceButton = (Button) container.findViewById(R.id.choice_select);
            TextView unitLabel = ((TextView) container.findViewById(R.id.field_unit));

            String safeValue = (!JSONObject.NULL.equals(value)) ? value.toString() : "";
            edit.setVisibility(View.VISIBLE);
            choiceButton.setVisibility(View.GONE);
            edit.setText(safeValue);
            this.valueView = edit;
            unitLabel.setText(this.unitText);

            if (this.format != null) {
                setFieldKeyboard(edit);
            }

            // Special case for tree diameter. Make a synced circumference
            // field
            if (this.key.equals(TREE_DIAMETER)) {
                return makeDynamicDbhFields(layout, context, container);
            }
        }

        return container;
    }

    /**
     * Explode tree.diameter to include a circumference field which can update
     * each other
     */
    private View makeDynamicDbhFields(LayoutInflater layout, Context context, View container) {
        container.setId(R.id.dynamic_dbh);
        View circ = layout.inflate(R.layout.plot_field_edit_row, null);

        circ.setId(R.id.dynamic_circumference);
        circ.findViewById(R.id.choice_select).setVisibility(View.GONE);
        ((TextView) circ.findViewById(R.id.field_label)).setText(R.string.circumference_label);
        setFieldKeyboard((EditText) circ.findViewById(R.id.field_value));
        ((TextView) container.findViewById(R.id.field_unit)).setText(this.unitText);

        LinearLayout dynamicDbh = new LinearLayout(context);
        dynamicDbh.setOrientation(LinearLayout.VERTICAL);
        dynamicDbh.addView(container);
        dynamicDbh.addView(circ);
        return dynamicDbh;
    }

    /**
     * Format the value with any units, if provided in the definition
     */
    @Override
    protected String formatUnit(Object value) {
        if (format != null) {
            if (format.equals("float")) {
                return formatWithDigits(value, this.digits) + " " + this.unitText;
            }
        }
        return value + " " + this.unitText;
    }

    @Override
    protected Object getEditedValue() {
        if (this.valueView != null) {
            // For proper JSON encoding of types, we'll use the keyboard type
            // to cast the edited value to the desired Java type. Choice buttons
            // are assumed to always be strings

            EditText textbox = (EditText) valueView;
            String text = textbox.getText().toString();

            if (TextUtils.isEmpty((textbox.getText().toString()))) {
                return null;
            }

            int inputType = textbox.getInputType();

            if ((inputType & InputType.TYPE_CLASS_TEXT) == InputType.TYPE_CLASS_TEXT) {
                return text;

            } else if ((inputType & InputType.TYPE_NUMBER_FLAG_DECIMAL) == InputType.TYPE_NUMBER_FLAG_DECIMAL) {
                return Double.parseDouble(text);
            } else if ((inputType & InputType.TYPE_CLASS_NUMBER) == InputType.TYPE_CLASS_NUMBER) {
                return Integer.parseInt(text);
            }

            return text;
        }
        return null;
    }

    private String formatWithDigits(Object value, int digits) {
        try { // attempt to round 'value'
            Double d = Double.parseDouble(value.toString());
            return String.format("%." + digits + "f", d);
        } catch (ClassCastException e) {
            return value.toString();
        }
    }

    private void setFieldKeyboard(EditText edit) {
        if (this.format.equals("float")) {
            edit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else if (this.format.equalsIgnoreCase("int")) {
            edit.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            edit.setInputType(InputType.TYPE_CLASS_TEXT);
        }
    }
}