package org.azavea.otm.adapters;

import android.app.Activity;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.azavea.otm.InstanceInfo;
import org.azavea.otm.R;

import java.util.ArrayList;
import java.util.Locale;

public class InstanceInfoArrayAdapter extends TwoArrayAdapter<InstanceInfo> {
    private Location userLocation;
    private LayoutInflater inflator;

    public InstanceInfoArrayAdapter(ArrayList<InstanceInfo> personal,
                                    ArrayList<InstanceInfo> nearby,
                                    Activity context,
                                    Location userLocation) {

        super(context, personal, nearby, "My Tree Maps", "Nearby Tree Maps");
        this.inflator = context.getLayoutInflater();
        this.userLocation = userLocation;
    }

    @Override
    protected View getInflatedSeparatorView() {
        return inflator.inflate(R.layout.instance_switcher_separator_row, null, true);
    }

    @Override
    protected TextView getSeparatorInnerTextView(View separatorView) {
        return (TextView)separatorView.findViewById(R.id.text);
    }

    @Override
    public View getElementView(int position, View convertView, ViewGroup parent) {
        InstanceInfo element = this.getItem(position);

        if (convertView == null) {
            convertView = inflator.inflate(R.layout.instance_switcher_element_row, null, true);
        }

        // make subviews for components of the listView row
        TextView textView = (TextView) convertView.findViewById(R.id.text);
        TextView subtextView = (TextView) convertView.findViewById(R.id.subtext);

        // gather data from instanceInfo in scope to use in subviews
        Location instanceLocation = new Location(userLocation);
        instanceLocation.setLatitude(element.getLat());
        instanceLocation.setLongitude(element.getLon());
        String distance = distanceToString(userLocation.distanceTo(instanceLocation), true) + " away";

        // populate subviews for the instance in scope
        textView.setText(element.getName());
        subtextView.setText(distance);

        return convertView;
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
