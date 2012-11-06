package org.azavea.otm.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Plot extends Model {
	public Plot() throws JSONException {
		data = new JSONObject();
	}
	
	public Plot(int id, long width, long length, String type,
			boolean readOnly, String powerlineConflictPotential,
			String sidewalkDamage, String addressStreet, String addressCity,
			String addressZip, String dataOwner, String lastUpdated,
			String lastUpdatedBy) throws JSONException {
		this();		
		
		setId(id);
		setWidth(width);
		setLength(length);
		setType(type);
		setReadOnly(readOnly);
		setPowerlineConflictPotential(powerlineConflictPotential);
		setSidewalkDamage(sidewalkDamage);
		setAddressStreet(addressStreet);
		setAddressCity(addressCity);
		setLastUpdated(lastUpdated);
		setLastUpdatedBy(lastUpdatedBy);
	}
	
	public int getId() throws JSONException {
		return data.getInt("id");
	}
	
	public void setId(int id) throws JSONException {
		data.put("id", id);
	}
	
	public long getWidth() throws JSONException {
		return getLongOrDefault("plot_width", 0l);
	}
	
	public void setWidth(long width) throws JSONException {
		data.put("plot_width", width);
	}
	
	public long getLength() throws JSONException {
		return getLongOrDefault("plot_length", 0l);
	}
	
	public void setLength(long length) throws JSONException {
		data.put("plot_length", length);
	}
	
	public String getType() throws JSONException {
		return data.getString("type");
	}
	
	public void setType(String type) throws JSONException {
		data.put("type", type);
	}
	
	public boolean isReadOnly() throws JSONException {
		return data.getBoolean("readonly");
	}
	
	public void setReadOnly(boolean readOnly) throws JSONException {
		data.put("readonly", readOnly);
	}
	
	public String getPowerlineConflictPotential() throws JSONException {
		return data.getString("power_lines");
	}
	
	public void setPowerlineConflictPotential(String powerlineConflictPotential) throws JSONException {
		data.put("power_lines", powerlineConflictPotential);
	}
	
	public String getSidewalkDamage() throws JSONException {
		return data.getString("sidewalk_damage");
	}
	
	public void setSidewalkDamage(String sidewalkDamage) throws JSONException {
		data.put("sidewalk_damage", sidewalkDamage);
	}
	
	public String getAddress() throws JSONException {
		return data.getString("address");
	}
	
	public void setAddress(String address) throws JSONException {
		data.put("address", address);
	}
	
	public String getAddressStreet() throws JSONException {
		return data.getString("address_street");
	}
	
	public void setAddressStreet(String addressStreet) throws JSONException {
		data.put("address_street", addressStreet);
	}
	
	public String getAddressCity() throws JSONException {
		return data.getString("address_city");
	}
	
	public void setAddressCity(String addressCity) throws JSONException {
		data.put("address_city", addressCity);
	}
	
	public String getAddressZip() throws JSONException {
		return data.getString("address_zip");
	}
	
	public void setAddressZip(String addressZip) throws JSONException {
		data.put("address_zip", addressZip);
	}
	
	public String getDataOwner() throws JSONException {
		return data.getString("data_owner");
	}
	
	public void setDataOwner(String dataOwner) throws JSONException {
		data.put("data_owner", dataOwner);
	}
	
	public String getLastUpdated() throws JSONException {
		return data.getString("last_updated");
	}
	
	public void setLastUpdated(String lastUpdated) throws JSONException {
		data.put("last_updated", lastUpdated);
	}
	
	public String getLastUpdatedBy() throws JSONException {
		return data.getString("last_updated_by");
	}
	
	public void setLastUpdatedBy(String lastUpdatedBy) throws JSONException {
		data.put("last_updated_by", lastUpdatedBy);
	}
	
	public Tree getTree() throws JSONException {
		if (data.isNull("tree")) {
			return null;
		}
		Tree retTree = new Tree();
		retTree.setData(data.getJSONObject("tree"));
		return retTree;
	}
	
	public void setTree(Tree tree) throws JSONException {
		data.put("tree", tree.getData());
	}
	
	public Geometry getGeometry() throws JSONException {
		Geometry retGeom = new Geometry();
		retGeom.setData(data.getJSONObject("geometry"));
		return retGeom;
	}
	
	public void setGeometry(Geometry geom) throws JSONException {
		data.put("geometry", geom.getData());
	}
	
	public boolean canEditPlot() throws JSONException {
		return data.getJSONObject("perm").getJSONObject("plot").getBoolean("can_edit");
	}
	
	public boolean canEditTree() throws JSONException {
		return data.getJSONObject("perm").getJSONObject("tree").getBoolean("can_edit");
	}
	
	public boolean canDeletePlot() throws JSONException {
		return data.getJSONObject("perm").getJSONObject("plot").getBoolean("can_delete");
	}
	
	public boolean canDeleteTree() throws JSONException {
		return data.getJSONObject("perm").getJSONObject("tree").getBoolean("can_delete");
	}	
}
