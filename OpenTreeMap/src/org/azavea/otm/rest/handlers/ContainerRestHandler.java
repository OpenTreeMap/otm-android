package org.azavea.otm.rest.handlers;

import org.azavea.otm.data.ModelContainer;
import org.json.JSONArray;

import com.loopj.android.http.JsonHttpResponseHandler;

@SuppressWarnings("rawtypes")
public class ContainerRestHandler<T extends ModelContainer> extends JsonHttpResponseHandler {
    private T resultObject;

    public ContainerRestHandler(T resultObject) {
        this.resultObject = resultObject;
    }

    @Override
    public void onSuccess(JSONArray response) {
        resultObject.setData(response);
        dataReceived(resultObject);
    }

    // Overridden by consuming class
    public void dataReceived(T responseObject) {
    }
}
