package org.azavea.otm.filters;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import org.azavea.helpers.Logger;
import org.azavea.otm.R;
import org.json.JSONException;
import org.json.JSONObject;


public abstract class RangeFilter<T> extends BaseFilter {
    private T min;
    private T max;

    interface GetValue {
        String get();
    }

    public RangeFilter(String key, String identifier, String label) {
        super(key, identifier, label);
    }

    protected abstract T valueFromView(@NonNull View view);

    protected abstract String valueToString(@NonNull T value);

    protected abstract @LayoutRes int getFieldResource();

    protected void onFieldLoaded(TextView field, Activity activity) {
        // Do Nothing
    }

    @Override
    public boolean isActive() {
        return (min != null || max != null);
    }

    @Override
    public View createView(LayoutInflater inflater, Activity activity) {
        View rangeControl = inflater.inflate(R.layout.filter_range_control, null);
        ((TextView) rangeControl.findViewById(R.id.filter_label)).setText(label);
        loadField(rangeControl, activity, R.id.min_stub, R.id.min, this::getMinString);
        loadField(rangeControl, activity, R.id.max_stub, R.id.max, this::getMaxString);
        return rangeControl;
    }

    private void loadField(View rangeControl, Activity activity, @IdRes int stubId,
                           @IdRes int viewId, GetValue getValue) {
        ViewStub stub = ((ViewStub) rangeControl.findViewById(stubId));
        stub.setLayoutResource(getFieldResource());
        stub.setOnInflateListener((s, control) -> {
            TextView view = (TextView) control;
            view.setText(getValue.get());
            onFieldLoaded(view, activity);
        });
        stub.setInflatedId(viewId);
        stub.inflate();
    }

    protected String getMinString() {
        return this.min == null ? "" : valueToString(this.min);
    }

    protected String getMaxString() {
        return this.max == null ? "" : valueToString(this.max);
    }


    @Override
    public void updateFromView(View view) {
        this.min = valueFromView(view.findViewById(R.id.min));
        this.max = valueFromView(view.findViewById(R.id.max));
    }

    @Override
    public void clear(View view) {
        this.min = null;
        this.max = null;
        for (int id : new int[]{R.id.min, R.id.max}) {
            ((TextView) view.findViewById(id)).setText("");
        }
    }

    @Override
    public JSONObject getFilterObject() {
        JSONObject filter = null;
        JSONObject predicateFilter = new JSONObject();

        try {
            if (min != null) {
                predicateFilter.put("MIN", this.min);
            }
            if (max != null) {
                predicateFilter.put("MAX", this.max);
            }
            if (predicateFilter.length() > 0) {
                filter = new JSONObject();
                filter.put(this.identifier, predicateFilter);
            }
        } catch (JSONException e) {
            Logger.error("Error creating JSONObject for filter", e);
        }

        return filter;
    }
}
