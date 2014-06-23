package org.azavea.map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.azavea.otm.App;
import org.azavea.otm.InstanceInfo;
import org.json.JSONArray;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

public class TMSTileProvider implements TileProvider {
    private final static int TILE_HEIGHT = 256;
    private final static int TILE_WIDTH = 256;

    //  OTM2 specific tile requests are in the format of:
    //    {georev}/database/otm/table/{feature}/{z}/{x}/{y}.png
    private static final String TILE_FORMAT = "%s/database/otm/table/%s/%d/%d/%d.png";

    // Url to the Tile Server
    private final String baseUrl; // http://example.com/tile/
    private final String featureName;
    private final int opacity;
    protected Set<String> displayList = new HashSet<>();

    public TMSTileProvider(String baseUrl, String featureName)
            throws MalformedURLException {
        this(baseUrl, featureName, 255);
    }

    public TMSTileProvider(String baseUrl, String featureName, int opacity)
            throws MalformedURLException {
        if (opacity < 0 || opacity > 255) {
            throw new IllegalArgumentException("opacity must be between 0 and 255");
        }
        this.featureName = featureName;
        this.baseUrl = new URL(baseUrl).toExternalForm();
        this.opacity = opacity;
    }

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

    @Override
    public Tile getTile(int x, int y, int zoom) {
        InputStream imageStream = null;
        Bitmap inputImage;
        try {
            imageStream = this.getTileUrl(x, y, zoom).openStream();
            inputImage = BitmapFactory.decodeStream(imageStream);
        } catch (IOException e) {
            Log.e(App.LOG_TAG, "Could not convert tiler results to Bitmap", e);
            return null;
        } finally {
            if (imageStream != null) {
                try {
                    imageStream.close();
                } catch (IOException e) {
                }
            }
        }
        if (inputImage == null) {
            return TileProvider.NO_TILE;
        }

        if (this.opacity == 255) {
            return bitmapToTile(inputImage);
        }

        Bitmap bmp = Bitmap.createBitmap(TILE_WIDTH, TILE_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setAlpha(this.opacity);
        canvas.drawBitmap(inputImage, 0, 0, paint);
        return bitmapToTile(bmp);
    }

    private Tile bitmapToTile(Bitmap bmp) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(bmp.getRowBytes() * bmp.getHeight());

        // Note: compress is a misnomer, since PNG is lossless.
        // Really, this is just a byte copy that retains header information, unlike copyPixelsToBuffer
        if (bmp.compress(CompressFormat.PNG, 100, buffer)) {
            return new Tile(TILE_WIDTH, TILE_HEIGHT, buffer.toByteArray());
        }
        return TileProvider.NO_TILE;
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
