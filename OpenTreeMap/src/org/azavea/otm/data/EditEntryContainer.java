package org.azavea.otm.data;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;

public class EditEntryContainer extends ModelContainer<EditEntry> {

	@Override
	public Map<Integer, EditEntry> getAll() throws JSONException{
		LinkedHashMap<Integer,EditEntry> entryList = 
				new LinkedHashMap<Integer,EditEntry>(data.length());
		for (int i = 0; i < data.length(); i++) {
			EditEntry entry = new EditEntry();
			entry.setData(data.getJSONObject(i));
			entryList.put(entry.getId(), entry);
		}
		return entryList;
	}
	
	public EditEntry getFirst() throws JSONException {
		EditEntry entry = null;
		if (data.length() > 0) {
			entry = new EditEntry();
			entry.setData(data.getJSONObject(0));
		}
		return entry;
	}
}	
