package org.azavea.otm.fields;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.Button;

import org.azavea.helpers.Logger;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Model;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.azavea.helpers.DateButtonListener.formatTimestampForDisplay;
import static org.azavea.helpers.DateButtonListener.getDateButtonListener;

public class DateField extends ButtonField {

    DateField(JSONObject fieldDef) {
        super(fieldDef);
    }

    /**
     * Format the value with any units, if provided in the definition
     */
    @Override
    protected String formatValue(Object value) {
        return formatTimestampForDisplay((String) value);
    }

    @Override
    protected void setupButton(final Button choiceButton, Object value, Model model, Activity activity) {
        if (!JSONObject.NULL.equals(value)) {
            final String timestamp = (String) value;
            final String formattedDate = formatTimestampForDisplay(timestamp);
            choiceButton.setText(formattedDate);
            choiceButton.setTag(R.id.choice_button_value_tag, timestamp);
        } else {
            choiceButton.setText(R.string.unspecified_field_value);
        }
        choiceButton.setOnClickListener(getDateButtonListener(activity, R.id.choice_button_value_tag));
    }
}