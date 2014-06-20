package org.azavea.otm.adapters;

import java.util.LinkedHashMap;
import java.util.Map;

import org.azavea.otm.data.Species;
import org.azavea.otm.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SpeciesAdapter extends ArrayAdapter<Species> {
    private static final int SECTIONED_CELL = 1;
    private static final int VALUE_CELL = 2;

    private Context context;
    private int layoutResourceId;
    private Species[] data = null;
    private Map<Character, Boolean> sections;
    private int[] sectionStates;

    public SpeciesAdapter(Context context, int layoutResourceId, Species[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.sections = createSections();
        sectionStates = new int[data.length];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SpeciesHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new SpeciesHolder();

            holder.separator = (TextView) row.findViewById(R.id.separator);
            holder.commonName = (TextView) row.findViewById(R.id.common_name);
            holder.scientificName = (TextView) row.findViewById(R.id.sci_name);

            row.setTag(holder);
        } else {
            holder = (SpeciesHolder) row.getTag();
        }

        // Use getItem as it's reference is always pointed to the data list
        // which is active. When filtering is applied, data is not active,
        // so data[position] will get the position from the unfiltered list.
        Species species = getItem(position);

        boolean needsSection = false;
        char section = species.getCommonName().toUpperCase().charAt(0);
        if (isNonAlpha(section)) {
            section = 'A';
        }

        // Check the cached section status for the cell in the current position
        // It is redrawn after the cell is removed from the screen
        switch (sectionStates[position]) {
        case SECTIONED_CELL:
            needsSection = true;
            break;
        case VALUE_CELL:
            needsSection = false;
            break;
        default:
            // The section status is unknown, so determine
            // if the first character of the name is already
            // a created section
            needsSection = !sections.get(section);

            // Cache this state for later renderings
            sectionStates[position] = needsSection ? SECTIONED_CELL
                    : VALUE_CELL;
            break;
        }

        // Check if the first letter of name has already been sectioned

        if (needsSection) {
            sections.put(section, true);
            holder.separator.setVisibility(View.VISIBLE);
            holder.separator.setText(Character.toString(section));

        } else {
            holder.separator.setVisibility(View.GONE);
        }

        holder.commonName.setText(species.getCommonName());
        holder.scientificName.setText(species.getScientificName());

        return row;
    }

    @Override
    public void notifyDataSetChanged() {
        // When the data set changes (from a filter), the section placements
        // need to be recalculated
        this.sections = createSections();
        this.sectionStates = new int[data.length];
        super.notifyDataSetChanged();
    }

    private Map<Character, Boolean> createSections() {
        Map<Character, Boolean> sections = 
                new LinkedHashMap<>(26);
        for (int i = 65; i <= 90; i++) {
            sections.put((char) i, false);
        }
        return sections;
    }

    static class SpeciesHolder {
        TextView separator;
        TextView commonName;
        TextView scientificName;
    }

    /*
     * Check if the character is a letter
     */
    boolean isNonAlpha(char c) {
        char tick = '\'';
        char qt = '"';

        return isDigit(c) || c == tick || c == qt;
    }

    boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }
}