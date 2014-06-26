package org.azavea.otm.adapters;

import java.util.LinkedHashMap;
import java.util.List;

import org.azavea.otm.data.Species;
import org.azavea.otm.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SpeciesAdapter extends LinkedHashMapAdapter<Species> {
    private final LayoutInflater inflater;

    public SpeciesAdapter(Context context, LinkedHashMap<CharSequence, List<Species>> data) {
        super(context, data);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getSeparatorView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SeparatorHolder holder;

        if (row == null) {
            row = inflater.inflate(R.layout.list_separator_row, parent, false);

            holder = new SeparatorHolder();
            holder.separator = (TextView) row.findViewById(R.id.separator);

            row.setTag(holder);
        } else {
            holder = (SeparatorHolder) row.getTag();
        }

        CharSequence text = getItem(position).key;

        holder.separator.setText(text);

        return row;
    }

    @Override
    public View getElementView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SpeciesHolder holder;

        if (row == null) {
            row = inflater.inflate(R.layout.species_list_row, parent, false);

            holder = new SpeciesHolder();

            holder.commonName = (TextView) row.findViewById(R.id.common_name);
            holder.scientificName = (TextView) row.findViewById(R.id.sci_name);

            row.setTag(holder);
        } else {
            holder = (SpeciesHolder) row.getTag();
        }

        Species species = getItem(position).value;

        holder.commonName.setText(species.getCommonName());
        holder.scientificName.setText(species.getScientificName());

        return row;
    }

    static class SpeciesHolder {
        TextView commonName;
        TextView scientificName;
    }

    static class SeparatorHolder {
        TextView separator;
    }
}