package org.azavea.otm.data;

import java.util.ArrayList;
import java.util.List;

import org.azavea.otm.App;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class PendingEditDescription {
	
	private JSONObject data;
	private String key;
	
	public PendingEditDescription(String key, JSONObject definition) {
		this.key = key;
		this.data = definition;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public String getLatestValue() {
		try {
			return data.getString("latest_value");
		} 
		catch (Exception e) {
			Log.e(App.LOG_TAG, "Unable to parse latest value for " + key, e);
			// The last approved value will show up if we return null
			return null;
		}
	}
	
	/**
	 * Get a list of all pending edits in order of submission
	 * @return
	 * @throws JSONException
	 */
	public List<PendingEdit> getPendingEdits() throws JSONException {
		JSONArray rawEdits = data.getJSONArray("pending_edits");
		List<PendingEdit> edits = new ArrayList<>(rawEdits.length());
		for (int i=0; i < rawEdits.length(); i++) {
			edits.add(new PendingEdit(rawEdits.getJSONObject(i)));
		}
		return edits;
	}
}
