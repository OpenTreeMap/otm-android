package org.azavea.otm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.azavea.otm.R;
import org.azavea.otm.data.Model;

import java.util.ArrayList;
import java.util.List;

public class TwoArrayAdapter<T extends Model> extends BaseAdapter {
    private ArrayList<T> data;

    private static final int ITEM_VIEW_TYPE_ELEMENT = 0;
    private static final int ITEM_VIEW_TYPE_SEPARATOR = 1;

    private String firstSeparatorText;
    private String secondSeparatorText;
    private Context context;

    public TwoArrayAdapter(Context context, List<T> array1, List<T> array2) {
        this(context, array1, array2, "Section 1", "Section 2");
    }

    public TwoArrayAdapter(Context context, List<T> array1, List<T> array2, String firstSeparatorText, String secondSeparatorText) {
        super();

        this.context = context;
        this.firstSeparatorText = firstSeparatorText;
        this.secondSeparatorText = secondSeparatorText;
        this.data = new ArrayList<T>();

        // make the data positionally match the way it'll look
        List<List<T>> arrays = new ArrayList<List<T>>();
        arrays.add(array1);
        arrays.add(array2);

        for (List<T> array : arrays) {
            if (!array.isEmpty()) {
                // add a separator for this array section
                this.data.add(null);
                this.data.addAll(array);
            }
        }
    }

    @Override
    public boolean areAllItemsEnabled () { return false; }

    @Override
    public int getViewTypeCount() { return 2; }

    @Override
    public int getCount() { return data.size(); }

    @Override
    public int getItemViewType(int position) {
        return data.get(position) == null ? ITEM_VIEW_TYPE_SEPARATOR : ITEM_VIEW_TYPE_ELEMENT;
    }

    @Override
    public T getItem(int position) { return data.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public boolean isEnabled(int position) { return data.get(position) != null; }

    public View getElementView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) { convertView = new TextView(context); }
        T element = getItem(position);
        ((TextView) convertView).setText(element.toString());
        return convertView;
    }

    public View getSeparatorView (int position, View convertView, ViewGroup parent) {
        if (convertView == null) { convertView = new TextView(context); }
        String sepText = position == 0 ? firstSeparatorText : secondSeparatorText;
        ((TextView) convertView).setText(sepText);
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == ITEM_VIEW_TYPE_SEPARATOR) {
            return getSeparatorView(position, convertView, parent);
        } else {
            return getElementView(position, convertView, parent);
        }
    }

}

