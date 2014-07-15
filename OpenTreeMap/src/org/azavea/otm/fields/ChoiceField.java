package org.azavea.otm.fields;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Button;

import org.azavea.otm.Choice;
import org.azavea.otm.R;
import org.azavea.otm.data.Model;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChoiceField extends ButtonField {
    // Any choices associated with this field, keyed by value with order preserved
    private final Map<String, Choice> choiceMap = new LinkedHashMap<>();

    // The order of values loaded into selection panel. Used to map index to keys in ChoiceMap
    private final ArrayList<String> choiceSelectionIndex = new ArrayList<>();
    private final ArrayList<String> choiceDisplayValues = new ArrayList<>();

    ChoiceField(JSONObject fieldDef) {
        super(fieldDef);
        JSONArray choices = fieldDef.optJSONArray("choices");

        if (choices != null) {
            for (int i = 0; i < choices.length(); i++) {
                JSONObject choiceDef = choices.optJSONObject(i);
                Choice choice = new Choice(choiceDef.optString("display_value"), choiceDef.optString("value"));

                // Dialog choice lists take only an array of strings,
                // and we must later get value by selection index
                choiceMap.put(choice.getValue(), choice);
                choiceSelectionIndex.add(choice.getValue());
                choiceDisplayValues.add(choice.getText());
            }
        }
    }

    /**
     * Format the value with any units, if provided in the definition
     */
    @Override
    public String formatUnit(Object value) {
        // If there are choices for this field, display the choice text, not the value
        Choice choice = this.choiceMap.get(value);
        if (choice != null) {
            return choice.getText();
        }
        return null;
    }

    @Override
    protected void setupButton(final Button choiceButton, Object value, Model model, Context context) {
        choiceButton.setText(R.string.unspecified_field_value);

        if (!JSONObject.NULL.equals(value)) {
            Choice currentChoice = choiceMap.get(value);
            if (!JSONObject.NULL.equals(currentChoice)) {
                choiceButton.setText(currentChoice.getText());
            }
        }

        choiceButton.setTag(R.id.choice_button_value_tag, value);

        handleChoiceDisplay(choiceButton, this);
    }

    private void handleChoiceDisplay(final Button choiceButton, final ChoiceField editedField) {
        choiceButton.setOnClickListener(view -> {
            // Determine which item should be selected by default
            Object currentValue = choiceButton.getTag(R.id.choice_button_value_tag);
            int checkedChoiceIndex = -1;

            if (!JSONObject.NULL.equals(currentValue)) {
                checkedChoiceIndex = editedField.choiceSelectionIndex.indexOf(currentValue);
            }

            new AlertDialog.Builder(choiceButton.getContext())
                    .setTitle(editedField.label)
                    .setSingleChoiceItems(editedField.choiceDisplayValues.toArray(new String[0]),
                            checkedChoiceIndex, (dialog, which) -> {
                                String displayText = editedField.choiceDisplayValues.get(which);
                                if (TextUtils.isEmpty(displayText)) {
                                    choiceButton.setText(R.string.unspecified_field_value);
                                } else {
                                    choiceButton.setText(displayText);
                                }
                                choiceButton.setTag(R.id.choice_button_value_tag,
                                        editedField.choiceSelectionIndex.get(which));
                                dialog.dismiss();
                            }
                    ).create().show();
        });
    }
}