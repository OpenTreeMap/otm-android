package org.azavea.otm.filters;

import android.support.annotation.IdRes;
import android.view.View;
import android.widget.EditText;

import org.azavea.otm.R;

public class NumericRangeFilter extends RangeFilter<Double> {
    public NumericRangeFilter(String key, String identifier, String label) {
        super(key, identifier, label);
    }

    @Override
    protected Double valueFromView(View view) {
        String min = ((EditText) view).getText().toString().trim();
        if (!"".equals(min)) {
            return Double.parseDouble(min);
        }
        return null;
    }

    @Override
    public String valueToString(Double value) {
        return Double.toString(value);
    }

    @Override
    protected int getFieldResource() {
        return R.layout.filter_numeric_range_control;
    }
}
