package org.azavea.otm.rest.handlers;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.azavea.otm.App;
import org.azavea.otm.data.Model;
import org.json.JSONObject;

public abstract class LoggingJsonHttpResponseHandler extends JsonHttpResponseHandler {

    @Override
    public void onFailure(int statusCode, Header[] headers, String message, Throwable e) {
        Log.w(App.LOG_TAG, String.format("Error in HTTP request. Status [%d] Message [%s]", statusCode, message), e);
        failure(e, message);
    }

    // JsonHttpResponseHandler turns an empty response body into a null JSONObject
    // so we just look for both String and JSON and send both to the same abstract method
    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject object) {
        final String jsonString = object != null ? object.toString() : "";
        onFailure(statusCode, headers, jsonString, e);
    }

    public abstract void failure(Throwable e, String message);
}
