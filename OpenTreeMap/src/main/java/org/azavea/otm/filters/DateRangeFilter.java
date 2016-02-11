package org.azavea.otm.filters;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.azavea.otm.R;

import static org.azavea.helpers.DateButtonListener.formatTimestampForDisplay;
import static org.azavea.helpers.DateButtonListener.getDateButtonListener;

public class DateRangeFilter extends RangeFilter<String> {
    public DateRangeFilter(String key, String identifier, String label) {
        super(key, identifier, label);
    }

    @Override
    protected String valueFromView(@NonNull View view) {
        return (String) view.getTag(R.id.date_filter_tag);
    }

    @Override
    public void clear(View view) {
        super.clear(view);
        for (int id : new int[]{R.id.min, R.id.max}) {
            view.findViewById(id).setTag(R.id.date_filter_tag, null);
        }
    }

    @Override
    public String valueToString(@NonNull String value) {
        return formatTimestampForDisplay(value);
    }

    @Override
    protected int getFieldResource() {
        return R.layout.filter_date_range_control;
    }

    @Override
    protected void onFieldLoaded(TextView field, Activity activity) {
        field.setOnClickListener(getDateButtonListener(activity, R.id.date_filter_tag));
    }
}
