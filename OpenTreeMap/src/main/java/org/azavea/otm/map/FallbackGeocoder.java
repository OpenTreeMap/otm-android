package org.azavea.otm.map;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.azavea.helpers.Logger;
import org.azavea.otm.data.InstanceInfo;
import org.azavea.otm.data.InstanceInfo.InstanceExtent;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

public class FallbackGeocoder {

    // search box
    private final InstanceInfo currentInstance;
    private final LatLngBounds extent;

    // activity context
    private final Context context;

    // the http client we are going to use to connect to google
    private final AsyncHttpClient client;

    // construct with a reference to the context and a search bounding box.
    public FallbackGeocoder(Context context, InstanceInfo currentInstance) {
        this.context = context;
        this.currentInstance = currentInstance;
        this.extent = currentInstance.getExtent();
        client = new AsyncHttpClient();
    }

    // *Attempt* to use android's native reverse geocoder
    //
    // numerous failure conditions can cause this method to return null.
    // best used in conjunction with the http geocoder as a fallback.
    //
    // due to a bug that surfaced in the android geocoder in 9/2014, this method
    // was modified to stop using extents to hint/bias the geocoder to favor the
    // region containing the instance. See this page for more details:
    // http://stackoverflow.com/questions/25621087/android-geocoder-getfromlocationname-stopped-working-with-bounds

    public LatLng androidGeocode(String addressText) {
        List<Address> addresses;
        Geocoder g = new Geocoder(this.context);
        try {
            addresses = g.getFromLocationName(addressText, 10);
        } catch (IOException e) {
            Logger.error("Geocoder exception", e);
            return null;
        }

        double instanceRadius, instanceLat, instanceLng;
        try {
            instanceRadius = currentInstance.getRadius();
            instanceLat = currentInstance.getLat();
            instanceLng = currentInstance.getLon();
        } catch (Exception e) {
            Logger.error("Required instance data not found. Exiting android geocoder", e);
            return null;
        }

        // addresses returned by the geocoder are sorted by match quality
        // return the first one that is within an acceptable distance
        for (Address address : addresses) {
            float[] results = new float[1];
            Location.distanceBetween(instanceLat, instanceLng, address.getLatitude(), address.getLongitude(), results);
            if (results[0] <= instanceRadius) {
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        }

        return null;
    }

    // Use Google's Http geocoder.
    public void httpGeocode(String address, JsonHttpResponseHandler handler) {
        String urlFormat = "http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=%s";
        String url;
        if (extent != null) {
            // Use this format to compose a url for google's geocode
            urlFormat += "&bounds=%f,%f%%7c%f,%f";
            // interpolate address and bounds into that format string
            url = String.format(Locale.US, urlFormat, URLEncoder.encode(address), extent.southwest.latitude,
                    extent.southwest.longitude, extent.northeast.latitude, extent.northeast.longitude);
        } else {
            url = String.format(Locale.US, urlFormat, URLEncoder.encode(address));
        }

        // execute the http request.
        client.get(url, new RequestParams(), handler);
    }

    // adapt a google JSON response from httpGeocode above into a
    // LatLng object.
    public static LatLng decodeGoogleJsonResponse(JSONObject json) {
        try {
            // parse out the lat/long from the first entry in the results
            // array.
            JSONObject location = json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry")
                    .getJSONObject("location");
            double lat = location.getDouble("lat");
            double lon = location.getDouble("lng");

            // If any of the above triggers an exception, or if we are null by
            // the
            // time we get to here, just return null.
            return new LatLng(lat, lon);
        } catch (Exception e) {
            Logger.error("Problem parsing geocoder response", e);
            return null;
        }
    }

}
