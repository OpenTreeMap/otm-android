package org.azavea.otm.filters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import org.azavea.helpers.Logger;
import org.azavea.otm.App;
import org.azavea.otm.Choice;
import org.azavea.otm.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChoiceFilter extends BaseFilter {
    private static final int DEFAULT = -1;

    protected Choice[] choices;
    protected int selectedIndex = DEFAULT;

    public ChoiceFilter(String key, String identifier, String label,
                        JSONArray choices) {
        super(key, identifier, label);
        this.choices = loadChoices(choices);
    }

    private Choice[] loadChoices(JSONArray choiceDefs) {

        ArrayList<Choice> choices = new ArrayList<>();
        for (int i = 0; i < choiceDefs.length(); i++) {
            try {
                JSONObject c = choiceDefs.getJSONObject(i);
                String display = c.getString("display_value");
                String value = c.getString("value");

                choices.add(new Choice(display, value));
            } catch (JSONException e) {
                Logger.error("Improperly configured choices for filter: " + this.identifier, e);
            }
        }
        // The Android UI doesn't need to display and empty value
        // which the API often returns as the first item
        if (TextUtils.isEmpty(choices.get(0).getText())) {
            choices.remove(0);
        }
        return choices.toArray(new Choice[choices.size()]);
    }

    @Override
    public boolean isActive() {
        return selectedIndex > DEFAULT;
    }

    @Override
    public View createView(LayoutInflater inflater, Activity activity) {
        View choiceLayout = inflater.inflate(R.layout.filter_choice_control, null);
        final Button choiceButton = (Button) choiceLayout.findViewById(R.id.choice_filter);

        choiceButton.setText(getSelectedLabelText());

        choiceButton.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(choiceButton
                    .getContext())
                    .setTitle(label)
                    .setSingleChoiceItems(getChoicesText(),
                            selectedIndex,
                            (dialog1, which) -> {
                                selectedIndex = which;
                                choiceButton.setText(getSelectedLabelText());
                                dialog1.dismiss();
                            }
                    ).create();

            String buttonLabel = App.getAppInstance().getString(R.string.choice_filter_clear);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, buttonLabel,
                    (dialog1, which) -> clear(choiceLayout));

            dialog.show();

        });

        return choiceLayout;
    }

    @Override
    public void updateFromView(View view) {
    }

    @Override
    public void clear(View view) {
        selectedIndex = DEFAULT;
        final Button button = (Button) view.findViewById(R.id.choice_filter);
        button.setText(getSelectedLabelText());
    }

    @Override
    public JSONObject getFilterObject() {
        return buildNestedFilter(this.identifier, "IS", choices[this.selectedIndex].getValue());
    }

    private String getSelectedValueText() {
        String text = "";
        if (isActive()) {
            Choice choice = this.choices[this.selectedIndex];
            if (choice != null) {
                text = choice.getText();
            }
        }
        return text;
    }

    private String getSelectedLabelText() {
        String labelText = this.label;
        if (isActive()) {
            labelText += ": " + getSelectedValueText();
        }
        return labelText;
    }

    /**
     * Get an array of the text of each choice value
     */
    private CharSequence[] getChoicesText() {
        String[] texts = new String[this.choices.length];
        for (int i = 0; i < this.choices.length; i++) {
            texts[i] = this.choices[i].getText();
        }
        return texts;
    }
}
