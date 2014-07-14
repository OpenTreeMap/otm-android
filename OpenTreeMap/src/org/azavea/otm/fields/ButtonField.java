package org.azavea.otm.fields;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.azavea.otm.R;
import org.azavea.otm.data.Model;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.User;
import org.json.JSONObject;


public abstract class ButtonField extends Field {
    public ButtonField(JSONObject fieldDef) {
        super(fieldDef);
    }

    @Override
    protected Object getEditedValue() {
        if (this.valueView != null) {
            Object choiceVal = this.valueView.getTag(R.id.choice_button_value_tag);

            if (JSONObject.NULL.equals(choiceVal) || TextUtils.isEmpty(choiceVal.toString())) {
                return null;
            }

            return choiceVal;
        }
        return null;
    }

    /*
     * Render a view to display the given model field in edit mode
     */
    @Override
    public View renderForEdit(LayoutInflater layout, Plot model, User user, Context context) {
        View container = null;

        if (this.canEdit) {
            container = layout.inflate(R.layout.plot_field_edit_button_row, null);
            Object value = getValueForKey(this.key, model.getData());

            ((TextView) container.findViewById(R.id.field_label)).setText(this.label);
            Button choiceButton = (Button) container.findViewById(R.id.choice_select);

            this.valueView = choiceButton;

            setupButton(choiceButton, value, model);
        }

        return container;
    }

    protected abstract void setupButton(Button button, Object value, Model model);
}