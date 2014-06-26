package org.azavea.otm.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import org.azavea.otm.R;
import org.azavea.otm.data.Model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class LinkedHashMapAdapter<T extends Model> extends BaseAdapter implements Filterable {

    private LinkedHashMap<CharSequence, List<T>> originalData = null;
    private List<Entry<T>> flattenedData;

    private static final int ITEM_VIEW_TYPE_ELEMENT = 0;
    private static final int ITEM_VIEW_TYPE_SEPARATOR = 1;

    private Filter filter = null;

    public LinkedHashMapAdapter(LinkedHashMap<CharSequence, List<T>> data) {
        this.originalData = data;
        this.flattenedData = getFlattenedList(data);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return flattenedData.size();
    }

    @Override
    public Entry<T> getItem(int position) {
        return flattenedData.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).value == null ? ITEM_VIEW_TYPE_SEPARATOR : ITEM_VIEW_TYPE_ELEMENT;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != ITEM_VIEW_TYPE_SEPARATOR;
    }

    public abstract View getElementView(int position, View convertView, ViewGroup parent);

    public abstract View getSeparatorView(int position, View convertView, ViewGroup parent);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == ITEM_VIEW_TYPE_SEPARATOR) {
            convertView = getSeparatorView(position, convertView, parent);
        } else {
            convertView = getElementView(position, convertView, parent);
        }

        return convertView;
    }

    /**
     * It is easier to work with a flattened list when building individual rows, but because the
     * data can be changed at any time from filtering, we need to reflatten the map on occasion
     *
     * @return a list of key and value pairs, with null values where each section header should go
     */
    private List<Entry<T>> getFlattenedList(LinkedHashMap<CharSequence, List<T>> data) {
        List<Entry<T>> list = new ArrayList<>();
        for (Map.Entry<CharSequence, List<T>> items : data.entrySet()) {
            if (!items.getValue().isEmpty()) {
                // null here signifies a separator row.
                list.add(new Entry<>(items.getKey(), null));
                for (T item : items.getValue()) {
                    list.add(new Entry<>(items.getKey(), item));
                }
            }
        }
        return list;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new LinkedHashMapFilter();
        }
        return filter;
    }

    /**
     * The below was adapted from the ArrayFilter inner class of ArrayAdapter, which is
     * unfortunately private.
     */
    private class LinkedHashMapFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results;

            LinkedHashMap<CharSequence, List<T>> newData;
            synchronized (this) {
                newData = new LinkedHashMap<>(originalData.size());
                for (CharSequence key : originalData.keySet()) {
                    // Make a copy of each list
                    newData.put(key, new ArrayList<>(originalData.get(key)));
                }
            }

            if (prefix == null || prefix.length() == 0) {
                results = getFilterResults(newData);
            } else {
                String prefixString = prefix.toString().toLowerCase();

                for (Map.Entry<CharSequence, List<T>> entry : newData.entrySet()) {
                    final List<T> values = entry.getValue();
                    final ArrayList<T> newValues = new ArrayList<>();

                    for (T value : values) {
                        final String valueText = value.toString().toLowerCase();

                        // First match against the whole, non-splitted value
                        if (valueText.startsWith(prefixString)) {
                            newValues.add(value);
                        } else {
                            final String[] words = valueText.split(" ");

                            // Start at index 0, in case valueText starts with space(s)
                            for (String word : words) {
                                if (word.startsWith(prefixString)) {
                                    newValues.add(value);
                                    break;
                                }
                            }
                        }
                    }
                    newData.put(entry.getKey(), newValues);
                }

                results = getFilterResults(newData);
            }

            return results;
        }

        private FilterResults getFilterResults(LinkedHashMap<CharSequence, List<T>> newData) {
            FilterResults results;
            results = new FilterResults();
            List<Entry<T>> flatList = getFlattenedList(newData);
            results.values = flatList;
            results.count = flatList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            flattenedData = (List<Entry<T>>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    public static class Entry<T> {
        public final CharSequence key;
        public final T value;

        public Entry(CharSequence key, T value) {
            this.key = key;
            this.value = value;
        }
    }
}

