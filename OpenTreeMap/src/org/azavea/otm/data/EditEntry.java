package org.azavea.otm.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class EditEntry extends Model {
	public EditEntry() {
		data = new JSONObject();
	}

	
	public int getPlotId() throws JSONException {
		return data.getInt("plot_id");
	}
	
	public Plot getPlot() throws JSONException {
		Plot plot = null;
		if (data.has("plot")) {
			plot = new Plot(data.getJSONObject("plot"));
		}
		return plot;
	}
	
	public int getId() throws JSONException {
		return data.getInt("id");
	}
	
	public String getName() throws JSONException {
		return data.getString("name");
	}

	public int getValue() throws JSONException {
		return data.getInt("value");
	}
	
	public Date getEditTime() throws JSONException {
		String created = data.getString("created");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
		try {
			return dateFormat.parse(created);
		} catch (ParseException e) {
			return null;
		}
		
	}
}
