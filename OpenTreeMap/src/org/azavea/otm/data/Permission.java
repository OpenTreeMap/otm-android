package org.azavea.otm.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Permission extends Model {
	public Permission() {
		data = new JSONObject();
	}
	
	public Permission(boolean canEdit, boolean canDelete) throws JSONException {
		this();
		setCanEdit(canEdit);
		setCanDelete(canDelete);
	}
	
	public boolean getCanEdit() throws JSONException {
		return data.getBoolean("can_edit");
	}
	
	public void setCanEdit(boolean canEdit) throws JSONException {
		data.put("can_edit", canEdit);
	}
	
	public boolean getCanDelete() throws JSONException {
		return data.getBoolean("can_delete");
	}
	
	public void setCanDelete(boolean canDelete) throws JSONException {
		data.put("can_delete", canDelete);
	}
}
