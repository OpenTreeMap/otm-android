package org.azavea.otm.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.azavea.otm.R;
import org.azavea.otm.data.Model;

public abstract class FilterableListDisplay<T extends Model> extends ListActivity {

    public static final String MODEL_DATA = "model";

    protected abstract int getFilterHintTextId();

    @Override
    public void onCreate(Bundle data) {
        super.onCreate(data);

        setContentView(R.layout.filterable_list_selector);

        EditText filterEditText = (EditText) findViewById(R.id.filter_text);
        filterEditText.setHint(getFilterHintTextId());
    }

    public void renderList(ArrayAdapter<T> adapter) {
        setListAdapter(adapter);
        setupFiltering(adapter);
    }

    private void setupFiltering(final ArrayAdapter<T> adapter) {
        EditText filterEditText = (EditText) findViewById(R.id.filter_text);
        setKeyboardChangeEvents(adapter, filterEditText);
        setTextWatcherEvents(adapter, filterEditText);
    }

    /**
     * Listen on events of the filter text box to pass along filter text
     * and invalidate the current view
     */
    private void setTextWatcherEvents(final ArrayAdapter<T> adapter,
                                      EditText filterEditText) {
        filterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }
        });
    }

    /**
     * Listen on events the keyboard emits when finished editing
     * ('Done' and 'Next' are the ENTER event).
     * The Keyboard ENTER event on a ListActivity will re-render
     * the list, so the current view must be invalidated
     */
    private void setKeyboardChangeEvents(final ArrayAdapter<T> adapter,
                                         EditText filterEditText) {
        filterEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                adapter.notifyDataSetChanged();
            }
            return false;
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Model selection = (Model) l.getItemAtPosition(position);
        Intent result = new Intent();

        result.putExtra(MODEL_DATA, selection.getData().toString());

        setResult(RESULT_OK, result);
        finish();
    }
}
