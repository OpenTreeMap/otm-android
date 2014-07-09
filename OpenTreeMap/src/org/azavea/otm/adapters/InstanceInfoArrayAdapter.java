package org.azavea.otm.adapters;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.azavea.otm.data.InstanceInfo;
import org.azavea.otm.R;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class InstanceInfoArrayAdapter extends LinkedHashMapAdapter<InstanceInfo> {
    private final Location userLocation;
    private final LayoutInflater inflator;

    public InstanceInfoArrayAdapter(LinkedHashMap<CharSequence, List<InstanceInfo>> instances,
                                    Context context, Location userLocation) {
        super(context, instances);
        this.inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.userLocation = userLocation;
    }

    @Override
    public View getSeparatorView(int position, View convertView, ViewGroup parent) {
        CharSequence key = getItem(position).key;
        return createViewFromResource(convertView, parent, R.layout.instance_switcher_separator_row, R.id.text, key);
    }

    @Override
    public View getElementView(int position, View convertView, ViewGroup parent) {
        InstanceInfo element = this.getItem(position).value;
        InstanceHolder holder;

        if (convertView == null) {
            convertView = inflator.inflate(R.layout.instance_switcher_element_row, parent, false);

            holder = new InstanceHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.text);
            holder.subtextView = (TextView) convertView.findViewById(R.id.subtext);

            convertView.setTag(holder);
        } else {
            holder = (InstanceHolder) convertView.getTag();
        }
        // gather data from instanceInfo in scope to use in subviews
        Location instanceLocation = new Location(userLocation);
        instanceLocation.setLatitude(element.getLat());
        instanceLocation.setLongitude(element.getLon());
        String distance = distanceToString(userLocation.distanceTo(instanceLocation), true) + " away";

        // populate subviews for the instance in scope
        holder.textView.setText(element.getName());
        holder.subtextView.setText(distance);

        return convertView;
    }

    private static class InstanceHolder {
        TextView textView;
        TextView subtextView;
    }

    // TODO: backport/refactor to helper lib
    private static String distanceToString(float distance) {
        return distanceToString(distance, false);
    }

    // TODO: backport/refactor to helper lib
    private static String distanceToString(float distance, boolean convertFeetToMiles) {
        String unit = "m";
        String text;
        Locale locale;

        if (distance < 0) {
            // caused by an exception
            text = "Distance unknown";
        } else {
            locale = Locale.getDefault();
            if (locale != null && locale.equals(Locale.US)) {

                distance = (float) (distance * 3.38084);
                if (convertFeetToMiles) {
                    distance = distance / 5280;
                    unit = "mi";
                } else {
                    unit = "ft";
                }
            }
            text = Math.round(distance) + unit;
        }
        return text;
    }
}
