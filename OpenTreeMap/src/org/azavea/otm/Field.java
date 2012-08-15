package org.azavea.otm;

import java.util.HashMap;

import org.azavea.otm.data.Model;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class Field {
	private HashMap<String,String> unitFormatter = new HashMap<String,String>(){
		private static final long serialVersionUID = 1L;

	{
		put("inch", "in");
		put("feet", "ft");
		put("meter", "m");
		put("mile", "mi");
	}};

	/**
	 * The property name from Plot which will contain the data to display
	 * or edit.  Nested resources are separated by '.' notation
	 */
	public String key;
	
	/**
	 * Label to identify the field on a view
	 */
	public String label;
	
	/**
	 * The minimum reputation points needed to edit this field
	 */
	public int minimumToEdit;
	
	/**
	 * How to format units 
	 */
	public String format;
	
	/**
	 * Field type as defined in the configuration XML
	 */
	private String type;
	
	/**
	 * The keyboard type to use when editing this field
	 */
	public String keyboardResource = "text";
	
	protected Field(String key, String label, int minimumToEdit, String keyboard, 
			String format, String type) {
		this.key = key;
		this.label = label;
		this.minimumToEdit = minimumToEdit;
		this.keyboardResource = keyboard;
		this.format = format;
		this.type = type;
	}
	
	public static Field makeField(Node fieldNode) {
		// Key, label and minToEdit are required
		String key = fieldNode.getAttributes().getNamedItem("key")
				.getNodeValue();
		String label = fieldNode.getAttributes().getNamedItem("label")
				.getNodeValue();
		int minimumToEdit = Integer.parseInt(fieldNode.getAttributes().
				getNamedItem("minimumToEdit").getNodeValue());
		
		// Keyboard style will default to text if it's not present
		Node node = fieldNode.getAttributes().getNamedItem("keyboard");
		String keyboardResource = node == null ? "text" : node.getNodeValue();	
		
		node = fieldNode.getAttributes().getNamedItem("format");
		String format = node == null ? null : node.getNodeValue();
		
		node = fieldNode.getAttributes().getNamedItem("type");
		String type = node == null ? null : node.getNodeValue();	
		
		if (type != null && type.equals("eco")) {
			return new EcoField(key, label, minimumToEdit, keyboardResource, format, type);
		} else {
			return new Field(key, label, minimumToEdit, keyboardResource, format, type);
		}
	}
	
	public View renderForDisplay(LayoutInflater layout, Model model) throws JSONException {
		View container = layout.inflate(R.layout.plot_field_row, null);
        ((TextView)container.findViewById(R.id.field_label)).setText(this.label);
        ((TextView)container.findViewById(R.id.field_value))
        	.setText(formatUnit(getValueForKey(this.key, model.getData())));
        return container;
	}
	
	public View renderForEdit() {
		return null;
	}
	
	/**
	 * Format the value with any units, if provided in the definition
	 */
	public String formatUnit(Object value) {
		if (format != null) {
			return value.toString() + " " + unitFormatter.get(format);
		}
		return value.toString();
	}
	
	protected Object getValueForKey(String key, JSONObject json) throws JSONException {
		String[] keys = key.split("\\.");
		return getValueForKey(keys, 0, json);
	}
	
	/**
	 * Return the value of a key name, which can be nested using . notation.  If the key does not 
	 * exist, will return an empty string
	 * @throws JSONException 
	 */
	private Object getValueForKey(String[] keys, int index, JSONObject json) throws JSONException {
		if (index < keys.length -1 && keys.length > 1) {
			JSONObject child = json.getJSONObject(keys[index]);
			index++;
			return getValueForKey(keys, index, child);
		}
		if (json.has(keys[index])) {
			return json.get(keys[index]); 
		}
		return "";
		
	}
}
