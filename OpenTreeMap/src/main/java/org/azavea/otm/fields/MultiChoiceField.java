package org.azavea.otm.fields;

import android.app.AlertDialog;
import android.text.TextUtils;
import android.widget.Button;

import org.azavea.otm.Choice;
import org.azavea.otm.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.azavea.helpers.JSONHelper.jsonStringArrayToList;

public class MultiChoiceField extends ChoiceField {
    MultiChoiceField(JSONObject fieldDef) {
        super(fieldDef);
    }

    @Override
    public String formatValue(Object valuesList) {
        List<String> values = jsonStringArrayToList((JSONArray) valuesList);
        Collection<String> labels = filter(transform(values, value -> {
            Choice choice = choiceMap.get(value);
            return choice != null ? choice.getText() : null;
        }), value -> value != null);

        if (!labels.isEmpty()) {
            return TextUtils.join(", ", values);
        }
        return null;
    }

    @Override
    protected void handleChoiceDisplay(final Button choiceButton, final ChoiceField editedField) {
        choiceButton.setOnClickListener(view -> {
            // Determine which items should be selected by default
            Object jsonValue = choiceButton.getTag(R.id.choice_button_value_tag);

            List<String> currentValues = JSONObject.NULL.equals(jsonValue)
                    ? newArrayList() :jsonStringArrayToList((JSONArray) jsonValue);

            boolean[] checkedChoices = new boolean[editedField.choiceDisplayValues.size()];
            for (String value : currentValues) {
                if (value == null) {
                    continue;
                }

                int valueIndex = editedField.choiceSelectionIndex.indexOf(value);
                if (valueIndex != -1) {
                    checkedChoices[valueIndex] = true;
                }
            }

            String[] choices = editedField.choiceDisplayValues.toArray(
                    new String[editedField.choiceDisplayValues.size()]);

            new AlertDialog.Builder(choiceButton.getContext())
                    .setTitle(editedField.label)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        if (currentValues.isEmpty()) {
                            choiceButton.setTag(R.id.choice_button_value_tag, null);
                            choiceButton.setText(formatValueIfPresent(null));
                        } else {
                            JSONArray newValues = new JSONArray(currentValues);
                            choiceButton.setTag(R.id.choice_button_value_tag, newValues);
                            choiceButton.setText(formatValueIfPresent(newValues));
                        }
                        dialog.dismiss();
                    })
                    .setMultiChoiceItems(choices, checkedChoices, (dialog, which, isChecked) -> {
                        String value = editedField.choiceSelectionIndex.get(which);
                        if (isChecked && !currentValues.contains(value)) {
                            currentValues.add(value);
                        } else if (!isChecked && currentValues.contains(value)) {
                            currentValues.remove(value);
                        }
                    })
                    .create().show();
        });
    }
}
