package org.azavea.otm.filters;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.azavea.otm.R;
import org.json.JSONObject;

public class TextFilter extends BaseFilter {
    private String text = null;

    public TextFilter(String key, String identifier, String label) {
        super(key, identifier, label);
    }

    @Override
    public boolean isActive() {
        return !TextUtils.isEmpty(text);
    }

    @Override
    public View createView(LayoutInflater inflater, Activity activity) {
        View filterLayout = inflater.inflate(R.layout.filter_text_control, null);
        ((TextView) filterLayout.findViewById(R.id.filter_label)).setText(label);
        ((TextView) filterLayout.findViewById(R.id.filter_text)).setText(text);
        return filterLayout;
    }

    @Override
    public void updateFromView(View view) {
        EditText textView = (EditText) view.findViewById(R.id.filter_text);
        text = textView.getText().toString();
    }

    @Override
    public void clear(View view) {
        text = null;
        EditText textView = (EditText) view.findViewById(R.id.filter_text);
        textView.setText("");
    }

    @Override
    public JSONObject getFilterObject() {
        return buildNestedFilter(identifier, "LIKE", text);
    }
}
