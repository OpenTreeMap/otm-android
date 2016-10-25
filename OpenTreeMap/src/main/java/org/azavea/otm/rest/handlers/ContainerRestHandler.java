package org.azavea.otm.rest.handlers;

import org.azavea.otm.data.ModelContainer;
import org.json.JSONArray;

import cz.msebera.android.httpclient.Header;

public abstract class ContainerRestHandler<T extends ModelContainer<?>> extends LoggingJsonHttpResponseHandler {
    private T resultObject;

    public ContainerRestHandler(T resultObject) {
        this.resultObject = resultObject;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
        resultObject.setData(response);
        dataReceived(resultObject);
    }

    // Overridden by consuming class
    public abstract void dataReceived(T responseObject);
}
