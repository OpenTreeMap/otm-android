package org.azavea.otm.filters;

import java.util.ArrayList;

import org.azavea.otm.App;
import org.azavea.otm.Choice;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

public class ChoiceFilter extends BaseFilter {
    public Choice[] choices;
    private int DEFAULT = -1;
    private int selectedIndex = DEFAULT;

    public ChoiceFilter(String key, String identifier, String label,
            JSONArray choices) {
        this.key = key;
        this.identifier = identifier;
        this.label = label;
        this.choices = loadChoices(choices);
    }

    private Choice[] loadChoices(JSONArray choiceDefs) {

        ArrayList<Choice> choices = new ArrayList<Choice>();
        for (int i = 0; i < choiceDefs.length(); i++) {
            try {
                JSONObject c = choiceDefs.getJSONObject(i);
                String display = c.getString("display_value");
                String value = c.getString("value");

                choices.add(new Choice(display, value));
            } catch (JSONException e) {
                Log.e(App.LOG_TAG, "Improperly configured choices for filter: "
                        + this.identifier, e);
                continue;
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
    public void updateFromView(View view) {
    }

    @Override
    public void clear() {
        this.selectedIndex = DEFAULT;
    }

    @Override
    public JSONObject getFilterObject() {
        return buildNestedFilter(this.identifier, "IS", choices[this.selectedIndex].getValue());
    }

    public Integer getSelectedIndex() {
        return this.selectedIndex;
    }

    public void setSelectedIndex(Integer index) {
        selectedIndex = index;
    }

    public String getSelectedValueText() {
        String text = "";
        if (isActive()) {
            Choice choice = this.choices[this.selectedIndex];
            if (choice != null) {
                text = choice.getText();
            }
        }
        return text;
    }

    public String getSelectedLabelText() {
        String labelText = this.label;
        if (isActive()) {
            labelText += ": " + getSelectedValueText();
        }
        return labelText;
    }

    /**
     * Get an array of the text of each choice value
     *
     */
    public CharSequence[] getChoicesText() {
        String[] texts = new String[this.choices.length];
        for (int i = 0; i < this.choices.length; i++) {
            texts[i] = this.choices[i].getText();
        }
        return texts;
    }
}
