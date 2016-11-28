package org.azavea.map;

import android.net.Uri;

import com.google.android.gms.maps.model.UrlTileProvider;

import org.azavea.otm.App;
import org.azavea.otm.data.InstanceInfo;
import org.json.JSONArray;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TMSTileProvider extends UrlTileProvider {
    private final static int TILE_HEIGHT = 256;
    private final static int TILE_WIDTH = 256;

    //  OTM2 specific tile requests are in the format of:
    //    {georev}/database/otm/table/{feature}/{z}/{x}/{y}.png
    private static final String TILE_FORMAT = "%s/database/otm/table/%s/%d/%d/%d.png";

    // Url to the Tile Server
    private final String baseUrl; // http://example.com/tile/
    private final String featureName;
    private Set<String> displayList = new HashSet<>();

    public TMSTileProvider(String baseUrl, String featureName)
            throws MalformedURLException {
        super(TILE_WIDTH, TILE_HEIGHT);
        this.featureName = featureName;
        this.baseUrl = new URL(baseUrl).toExternalForm();
    }

    @Override
    public URL getTileUrl(int x, int y, int zoom) {
        InstanceInfo instance = App.getAppInstance().getCurrentInstance();
        String displayList = new JSONArray(this.displayList).toString();

        String urlString = baseUrl + String.format(TILE_FORMAT, instance.getGeoRevId(), this.featureName, zoom, x, y);
        Uri.Builder urlBuilder = Uri.parse(urlString).buildUpon();
        urlBuilder.appendQueryParameter("show", displayList);
        urlBuilder.appendQueryParameter("instance_id", Integer.toString(instance.getInstanceId()));

        URL url;
        try {
            url = new URL(urlBuilder.build().toString());
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
        return url;
    }

    /**
     * Sets the display filters
     *
     * @param models: the models to show on the map
     */
    public void setDisplayParameters(Collection<String> models) {
        this.displayList = new HashSet<>(models);
    }
}
