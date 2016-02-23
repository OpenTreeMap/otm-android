package org.azavea.helpers;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.widget.Button;

import org.azavea.otm.App;
import org.azavea.otm.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateButtonListener {
    @NonNull
    public static Button.OnClickListener getDateButtonListener(Activity activity, @IdRes int tag) {
        return view -> {
            final Button button = (Button) view;
            final String setTimestamp = (String) button.getTag(tag);
            final Calendar cal = getCalendarForTimestamp(activity, setTimestamp);
            final DatePickerDialog dialog = new DatePickerDialog(activity, (v, year, month, day) -> {
                final String updatedTimestamp = getTimestamp(activity, year, month, day);
                final String displayDate = formatTimestampForDisplay(updatedTimestamp);

                button.setText(displayDate);
                button.setTag(tag, updatedTimestamp);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            if (setTimestamp != null) {
                dialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, activity.getString(R.string.date_field_clear), (d, which) -> {
                    button.setTag(tag, null);
                    button.setText(activity.getString(R.string.unspecified_field_value));
                });
            }
            dialog.show();
        };
    }

    public static Calendar getCalendarForTimestamp(Context context, String setTimestamp) {
        final Calendar cal = new GregorianCalendar();
        final SimpleDateFormat timestampFormatter =
                new SimpleDateFormat(context.getString(R.string.server_date_format));

        if (setTimestamp != null) {

            try {
                cal.setTime(timestampFormatter.parse(setTimestamp));
            } catch (ParseException e) {
                Logger.error("Error parsing date stored on tag.", e);
            }
        }
        return cal;
    }

    public static String getTimestamp(Context context, int year, int month, int day) {
        final SimpleDateFormat timestampFormatter =
                new SimpleDateFormat(context.getString(R.string.server_date_format));
        final Calendar updatedCal = new GregorianCalendar();
        updatedCal.set(Calendar.YEAR, year);
        updatedCal.set(Calendar.MONTH, month);
        updatedCal.set(Calendar.DAY_OF_MONTH, day);

        return timestampFormatter.format(updatedCal.getTime());
    }

    public static String formatTimestampForDisplay(String timestamp) {
        final String displayPattern = App.getCurrentInstance().getShortDateFormat();
        final String serverPattern = App.getAppInstance().getString(R.string.server_date_format);

        final SimpleDateFormat timestampFormatter = new SimpleDateFormat(serverPattern);
        final SimpleDateFormat displayFormatter = new SimpleDateFormat(displayPattern);
        try {
            final Date date = timestampFormatter.parse(timestamp);
            return displayFormatter.format(date);
        } catch (ParseException e) {
            Logger.warning("Problem parsing date", e);
            return App.getAppInstance().getResources().getString(R.string.unspecified_field_value);
        }
    }
}
