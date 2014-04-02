package org.azavea.otm.filters;

import org.azavea.otm.R;

import com.loopj.android.http.RequestParams;

import android.view.View;
import android.widget.EditText;

public class RangeFilter extends BaseFilter {
    final private double DEFAULT = 0;
    private double min = DEFAULT;
    private double max = DEFAULT;

    public RangeFilter(String key, String identifier, String label) {
        this.key = key;
        this.identifier = identifier;
        this.label = label;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    @Override
    public boolean isActive() {
        return (min > DEFAULT || max > DEFAULT) ? true : false;
    }

    @Override
    public void updateFromView(View view) {
        String min = ((EditText) view.findViewById(R.id.min)).getText()
                .toString().trim();
        String max = ((EditText) view.findViewById(R.id.max)).getText()
                .toString().trim();

        if (min != null && !"".equals(min)) {
            this.min = Double.parseDouble(min);
        } else {
            this.min = DEFAULT;
        }

        if (max != null && !"".equals(max)) {
            this.max = Double.parseDouble(max);
        } else {
            this.max = DEFAULT;
        }
    }

    @Override
    public void clear() {
        min = DEFAULT;
        max = DEFAULT;
    }

    // diameter_range=2-4.... diameter_range=0-8.... diameter_range=3-9999.....
    private String queryValue() {
        String qval = "";
        if (min > 0) {
            qval += Double.toString(min);
        } else {
            qval += "0";
        }
        qval += "-";
        if (max > 0) {
            qval += Double.toString(max);
        } else {
            qval += "999999";
        }
        return qval;
    }

    @Override
    public void addToCqlRequestParams(RequestParams rp) {
        // TODO Not Implemented
    }

    @Override
    public void addToNearestPlotRequestParams(RequestParams rp) {
        // TODO Not Implemented
    }
}
