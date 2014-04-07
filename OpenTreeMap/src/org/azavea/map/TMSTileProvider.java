package org.azavea.map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.azavea.otm.App;
import org.azavea.otm.InstanceInfo;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

public class TMSTileProvider implements TileProvider {
    private final static int TILE_HEIGHT = 256;
    private final static int TILE_WIDTH = 256;

    // Url to the Tile Server
    private String baseUrl; // http://example.com/tile/
    private String featureName;
    private int opacity;

    //  OTM2 specific tile requests are in the format of:
    //    {georev}/database/otm/table/{feature}/{z}/{x}/{y}.png
    private static final String TILE_FORMAT = "%s/database/otm/table/%s/%d/%d/%d.png";

    private JSONObject parameters = new JSONObject();

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

    private String formatTileUrl(int zoom, int x, int y) {
        InstanceInfo instance = App.getAppInstance().getCurrentInstance();
        String url =  baseUrl + String.format(TILE_FORMAT, instance.getGeoRevId(),
                this.featureName, zoom, x, y);

        url += "?instance_id=" + instance.getInstanceId();
        String filters = this.parameters.toString().replace("\\", "");
        try {
            url += "&q=" + URLEncoder.encode(filters, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d("TILER-TMS", url);
        return url;
    }

    public void setParameter(String key, String value) {
        try {
            this.parameters.put(key, value);
        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Bad parameter", e);
        }
    }

    public void setRangeParameter(String key, String min, String max) {
        JSONObject vals = new JSONObject();
        try {
            vals.put("MIN", min);
            vals.put("MAX", max);
            this.parameters.put(key, vals);
        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Bad range parameter", e);
        }
    }

    /***
     * Remove any query string parameters from the tile requests
     */
    public void clearParameters() {
        this.parameters = new JSONObject();
    }

    public URL getTileUrl(int x, int y, int zoom) {
        URL url = null;
        try {
            url = new URL(formatTileUrl(zoom, x, y));
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
                } catch (IOException e) { }
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
        if(bmp.compress(CompressFormat.PNG, 100, buffer)){
            return new Tile(TILE_WIDTH, TILE_HEIGHT, buffer.toByteArray());
        }
        return TileProvider.NO_TILE;
    }
}
