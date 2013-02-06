package org.azavea.otm.data;

import java.util.ArrayList;

import org.azavea.otm.App;
import org.azavea.otm.rest.RequestGenerator;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.loopj.android.http.BinaryHttpResponseHandler;

public class Plot extends Model {
	
	private PendingStatus hasPending = PendingStatus.Unset;
	
	enum PendingStatus {
		Pending,
		NoPending,
		Unset
	}
	
	/**
	 * When Requesting a plot tree photo, these are the valid image types
	 */
	public static String[] IMAGE_TYPES = new String[] { 
		"image/jpeg", "image/png", "image/gif" 
	};
	
	public Plot() {
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
		data.put("address_street", address);
		data.put("edit_address_street", address);
		data.put("geocode_address", address);
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
		Tree retTree = new Tree(this);
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
		return getPermission("plot", "can_edit");
	}
	
	public boolean canEditTree() throws JSONException {
		return getPermission("tree", "can_edit");
	}
	
	public boolean canDeletePlot() throws JSONException {
		return getPermission("plot", "can_delete");
	}
	
	public boolean canDeleteTree() throws JSONException {
		return getPermission("tree", "can_delete");
	}
	
	/***
	 * Does this plot have current pending edits?
	 * @throws JSONException
	 */
	public boolean hasPendingEdits() throws JSONException {
		// Use the cache if available, this might be called a lot
		if (hasPending != PendingStatus.Unset) {
			return hasPending == PendingStatus.Pending;
		}
		
		boolean pendings = false;
		if (!data.isNull("pending_edits")) {
			if (data.getJSONObject("pending_edits").length() > 0) {
				pendings = true;
			}
		}
		
		// Cache for this instance
		hasPending = pendings ? PendingStatus.Pending : PendingStatus.NoPending;
		return pendings;
	}
	
	/**
	 * Get a pending edit description for a given field key for a plot or
	 * tree
	 * @param key - name of field key
	 * @return An object representing a pending edit description for the field, or
	 * null if there are no pending edits
	 * @throws JSONException
	 */
	public PendingEditDescription getPendingEditForKey(String key) throws JSONException {
		if (this.hasPendingEdits()) {
			JSONObject edits = data.getJSONObject("pending_edits");
			if (!edits.isNull(key)) {
				return new PendingEditDescription(key, edits.getJSONObject(key));
			}
		}
		return null;
	}
	
	/**
	 * Get a plot or tree permission from a plot json
	 * @param name: "tree" or "plot"
	 * @param editType "can_edit" or "can_delete"
	 * @return
	 */
	private boolean getPermission(String name, String editType) {
		try {
			if (data.has("perm")) {
				JSONObject perm = data.getJSONObject("perm");
				
				if (perm.has(name)) {
					JSONObject json = perm.getJSONObject(name);
					return json.getBoolean(editType);
				}
			}
			return false;
		} catch (JSONException e) {
			return false;
		}
	}

	public boolean hasTree() {
		return !data.isNull("tree");
	}

	public void createTree() throws JSONException {
		this.setTree(new Tree());
	}
		
	/**
	 * Get the most recent tree photo for this plot, by way of an asynchronous
	 * response handler.  Use static helper methods on Plot to decode a photo 
	 * response
	 * @param binary image handler which will receive callback from
	 * async http request
	 * @throws JSONException
	 */
	public void getTreePhoto(BinaryHttpResponseHandler handler) throws JSONException{
		// TODO: If there is no tree, should we auto call fail on the handler?
		if (this.hasTree()) {
			ArrayList<Integer> imageIds = this.getTree().getImageIdList();
			if (imageIds != null && imageIds.size() > 0) {
				// Always retrieve the most recent image
				int imageId = imageIds.get(imageIds.size() - 1).intValue();
				
				RequestGenerator rg = new RequestGenerator();
				rg.getImage(this.getId(), imageId, handler);
			}
		}
	}
	
	/**
	 * Create standard sized bitmap image from tree photo data
	 * @param imageData
	 * @return
	 */
	public static Bitmap createTreeThumbnail(byte[] imageData) {
		Bitmap image = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
		// Use an 80x80px image thumbnail
		return Bitmap.createScaledBitmap(image, 80, 80, true);
	}

	public void assignNewTreePhoto(String title, int photoId) throws JSONException {
		Tree tree = this.getTree();
		if (tree != null) {
			JSONObject image = new JSONObject();
			image.put("title", title);
			image.put("id", photoId);
			
			tree.addImageToList(image);
		}
	}

	
}
