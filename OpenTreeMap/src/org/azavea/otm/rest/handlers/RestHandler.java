package org.azavea.otm.rest.handlers;

import org.azavea.otm.data.Model;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

public class RestHandler<T extends Model> extends JsonHttpResponseHandler {
	private T resultObject;
	
	public RestHandler(T resultObject) {
		this.resultObject = resultObject;
	}
	
	@Override
	public void onSuccess(JSONObject response) {
		resultObject.setData(response);
		dataReceived(resultObject);
	}

	// Overridden by consuming class
	public void dataReceived(T responseObject) {
		
	}
}
