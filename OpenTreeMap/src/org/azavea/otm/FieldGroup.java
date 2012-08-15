package org.azavea.otm;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.azavea.otm.data.Model;
import org.json.JSONException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FieldGroup {
	
	private String title;
	private Map<String,Field> fields = new LinkedHashMap<String, Field>();
	
	public FieldGroup() {
		this.title = "";
	}
	
	public FieldGroup(String title) {
		this.title = title;
	}
	
	public void addFields(Map<String,Field> fields) {
		this.fields = fields;
	}

	public void addFields(NodeList fieldDefs) {
		if (fieldDefs != null) {
			for (int i=0; i < fieldDefs.getLength(); i++) {
				Node def = fieldDefs.item(i);
				if (def.getNodeType() == Node.ELEMENT_NODE) {
					addField(Field.makeField(def));
				}
			}
		}
	}
	
	public void addField(Field field) {
		this.fields.put(field.key, field);
	}

	public String getTitle() {
		return title;
	}

	public Map<String,Field> getFields() {
		return fields;
	}

	/**
	 * Render a field group and its child fields for viewing
	 * @throws JSONException 
	 */
	public View renderForDisplay(LayoutInflater layout, Model model) throws JSONException {
		View container = layout.inflate(R.layout.plot_field_group, null);
		LinearLayout group = (LinearLayout)container.findViewById(R.id.field_group); 
        ((TextView)group.findViewById(R.id.group_name)).setText(this.title);
		for (Entry<String, Field> field : fields.entrySet()) {
			group.addView(field.getValue().renderForDisplay(layout, model));
		}
        return group;
	}
}
