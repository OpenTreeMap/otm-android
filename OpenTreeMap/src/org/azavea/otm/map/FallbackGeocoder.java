package org.azavea.otm.map;

import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import org.azavea.otm.InstanceInfo.InstanceExtent;
import org.json.JSONObject;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class FallbackGeocoder {

    // search box
    private final InstanceExtent extent;

    // activity context
    private final Context context;

    // the http client we are going to use to connect to google
    private final AsyncHttpClient client;

    // construct with a reference to the context and a search bounding box.
    public FallbackGeocoder(Context context, InstanceExtent extent) {
        this.context = context;
        this.extent = extent;
        client = new AsyncHttpClient();
    }

    // Use android's native geocoder.
    public LatLng androidGeocode(String address) {
        Geocoder g = new Geocoder(this.context);
        try {
            List<Address> a;
            if (extent != null) {
                a = g.getFromLocationName(address, 1, extent.minLatitude, extent.minLongitude, extent.maxLatitude,
                        extent.maxLongitude);
            } else {
                a = g.getFromLocationName(address, 1);
            }
            if (a.size() == 0) {
                return null;
            } else {
                Address geocoded = a.get(0);
                return new LatLng(geocoded.getLatitude(), geocoded.getLongitude());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Use Google's Http geocoder.
    public void httpGeocode(String address, JsonHttpResponseHandler handler) {
        String urlFormat = "http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=%s";
        String url;
        if (extent != null) {
            // Use this format to compose a url for google's geocode
            urlFormat += "&bounds=%f,%f%%7c%f,%f";
            // interpolate address and bounds into that format string
            url = String.format(Locale.US, urlFormat, URLEncoder.encode(address), extent.minLatitude,
                    extent.minLongitude, extent.maxLatitude, extent.maxLongitude);
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
            e.printStackTrace();
            return null;
        }
    }

}
