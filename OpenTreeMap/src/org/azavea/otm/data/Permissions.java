package org.azavea.otm.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Permissions extends Model {
	public Permissions() {
		data = new JSONObject();
	}
	
	public Permissions(Permission plot, Permission tree) throws JSONException {
		this();
		setPlotPerm(plot);
		setTreePerm(tree);
	}
	
	public Permission getPlotPerm() throws JSONException {
		Permission plotPerm = new Permission();
		plotPerm.setData(data.getJSONObject("plot"));
		return plotPerm;
	}
	
	public void setPlotPerm(Permission plot) throws JSONException {
		data.put("plot", plot.getData());
	}
	
	public Permission getTreePerm() throws JSONException {
		Permission treePerm = new Permission();
		treePerm.setData(data.getJSONObject("tree"));
		return treePerm;
	}
	
	public void setTreePerm(Permission tree) throws JSONException {
		data.put("tree", tree.getData());
	}
}
