package org.azavea.otm;

import java.util.HashMap;

import org.azavea.otm.data.Model;
import org.azavea.otm.data.User;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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
	public String type;
	
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
	
	public View renderForEdit(LayoutInflater layout, Model model, User user) throws JSONException {
		View container = null;
		
		if (user.getReputation() >= this.minimumToEdit) {
			container = layout.inflate(R.layout.plot_field_edit_row, null);
			Object value = getValueForKey(this.key, model.getData());
	        ((TextView)container.findViewById(R.id.field_label)).setText(this.label);
	        EditText edit = ((EditText)container.findViewById(R.id.field_value));
	        edit.setText(value != null ? value.toString() : "" );
	        
	        // InputType is not an enum, so we can't parse arbitrary text to get a value.  Therefore
	        // we have to manually support specific instances
	        
	        if (this.keyboardResource.equals("numberDecimal")) {
        		edit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
	        } else if (this.keyboardResource.equalsIgnoreCase("number")) {
	        	edit.setInputType(InputType.TYPE_CLASS_NUMBER);
	        } else if (this.keyboardResource.equalsIgnoreCase("text")) {
	        	edit.setInputType(InputType.TYPE_CLASS_TEXT);
	        } else if (this.keyboardResource.equalsIgnoreCase("dateTime")) {
	        	edit.setInputType(InputType.TYPE_CLASS_DATETIME);
	        } 
	        
		}
        
		return container;
	}
	
	/**
	 * Format the value with any units, if provided in the definition
	 */
	public String formatUnit(Object value) {
		// If there is no value, return an unspecified value
		if (value == null) {
			return App.getInstance().getResources()
					.getString(R.string.unspecified_field_value);
		} else if (format != null) {
			return value.toString() + " " + unitFormatter.get(format);
		}
		return value.toString();
	}

	/**
	 * Return the value of a key name, which can be nested using . notation.  If the key does not 
	 * exist, will return an empty string
	 * @throws JSONException 
	 */
	protected Object getValueForKey(String key, JSONObject json) {
		try {
			String[] keys = key.split("\\.");
			return getValueForKey(keys, 0, json);
		} catch (Exception e) {
			Log.w(App.LOG_TAG, "Could not find key: " + key + " on plot/tree object");
			return null;
		}
	}
	
	/**
	 * Return value for keys, which could be nested as an array
	 */
	private Object getValueForKey(String[] keys, int index, JSONObject json) throws JSONException {
		if (index < keys.length -1 && keys.length > 1) {
			JSONObject child = json.getJSONObject(keys[index]);
			index++;
			return getValueForKey(keys, index, child);
		}
		
		if (json.isNull(keys[index])) {
			return null;
		}
		return json.get(keys[index]); 
		
	}
}
