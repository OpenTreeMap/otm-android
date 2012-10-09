package org.azavea.otm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.azavea.otm.data.Model;
import org.azavea.otm.data.User;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

	// If this field has a choice list, these are the strings to display as choices
	private ArrayList<String> choiceItems = new ArrayList<String>();
	
	// If this field has a choice list, these are the integer values 
	// associated with the choice
	private ArrayList<Integer> choiceValues = new ArrayList<Integer>();
	
	// This is the view control, either button or EditText, which has the user value
	private View valueView = null;
	
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
	
	/**
	 * The choice name if this is a user select-able field
	 */
	public String choiceName = null;
	
	protected Field(String key, String label, int minimumToEdit, String keyboard, 
			String format, String type, String choice) {
		this.key = key;
		this.label = label;
		this.minimumToEdit = minimumToEdit;
		this.keyboardResource = keyboard;
		this.format = format;
		this.type = type;
		this.choiceName = choice;
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
		
		node = fieldNode.getAttributes().getNamedItem("choice");
		String choice = node == null ? null : node.getNodeValue();
		
		if (type != null && type.equals("eco")) {
			return new EcoField(key, label, minimumToEdit, keyboardResource, format, type);
		} else {
			return new Field(key, label, minimumToEdit, keyboardResource, format, type, choice);
		}
	}
	
	public View renderForDisplay(LayoutInflater layout, Model model) throws JSONException {
		View container = layout.inflate(R.layout.plot_field_row, null);
        ((TextView)container.findViewById(R.id.field_label)).setText(this.label);
        ((TextView)container.findViewById(R.id.field_value))
        	.setText(formatUnit(getValueForKey(this.key, model.getData())));
        return container;
	}
	
	public View renderForEdit(LayoutInflater layout, Model model, User user) 
			throws JSONException {
		
		View container = null;
		
		if (user.getReputation() >= this.minimumToEdit) {
			container = layout.inflate(R.layout.plot_field_edit_row, null);
			Object value = getValueForKey(this.key, model.getData());
			
	        ((TextView)container.findViewById(R.id.field_label)).setText(this.label);
	        EditText edit = (EditText)container.findViewById(R.id.field_value);
	        Button choiceButton = (Button)container.findViewById(R.id.choice_select);
	        
	        // Show the correct type of input for this field
	        if (this.choiceName != null) {
	        	edit.setVisibility(View.GONE);
	        	choiceButton.setVisibility(View.VISIBLE);
	        	this.valueView = choiceButton;
	        	setupChoiceDisplay(choiceButton, value);
	        } else {
	        	String safeValue = value != null ? value.toString() : ""; 
	        	edit.setVisibility(View.VISIBLE);
	        	choiceButton.setVisibility(View.GONE);
		        edit.setText(safeValue);
	        	this.valueView = edit;
	        	
	        	if (this.keyboardResource != null) {
					setFieldKeyboard(edit); 
		        }
	        }
		}
        
		return container;
	}

	private void setupChoiceDisplay(final Button choiceButton, Object value) {
		Choices choices = App.getFieldManager().getChoicesByName(this.choiceName);
		Map<Integer,Choice> choiceMap = choices.getChoices();
		
		String label = "Unspecified";
		final int v = value == null ? -1 : Integer.parseInt(value.toString()); 
		
		if (value != null) {
			Choice currentChoice = choiceMap.get(v);
			if (currentChoice != null) {
				label = currentChoice.getText();
			}
		}
    	choiceButton.setText(label);
    	choiceButton.setTag(R.id.choice_button_value_tag, value);
			
		for (Entry<Integer, Choice> entry: choiceMap.entrySet()) {
			this.choiceItems.add(entry.getValue().getText());
			this.choiceValues.add(entry.getKey());
		}

		handleChoiceDisplay(choiceButton, this);
		
	}

	private void handleChoiceDisplay(final Button choiceButton,
			final Field editedField) {
		choiceButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// Determine which item should be selected by default
				Object currentValue = choiceButton.getTag(R.id.choice_button_value_tag);
				int checkedChoiceIndex = -1;

				if (currentValue != null) {
					int val = Integer.parseInt(currentValue.toString());					
					checkedChoiceIndex = editedField.choiceValues.indexOf(val);
				} 

				new AlertDialog.Builder(choiceButton.getContext())
					.setTitle(editedField.label)
					.setSingleChoiceItems(editedField.choiceItems.toArray(new String[0]), checkedChoiceIndex, 
							new DialogInterface.OnClickListener() {
						
							@Override
							public void onClick(DialogInterface dialog, int which) {
								choiceButton.setText(editedField.choiceItems.get(which));
								choiceButton.setTag(R.id.choice_button_value_tag, editedField.choiceValues.get(which));
								dialog.dismiss();
							}
						})
					.create().show();				
			}
		});
	}

	private void setFieldKeyboard(EditText edit) {
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
			NestedJsonAndKey found = getValueForKey(keys, 0, json);
			if (found != null) {
				return found.get();
			} else {
				return null;
			}
		} catch (Exception e) {
			Log.w(App.LOG_TAG, "Could not find key: " + key + " on plot/tree object");
			return null;
		}
	}
	
	/**
	 * Return value for keys, which could be nested as an array
	 */
	private NestedJsonAndKey getValueForKey(String[] keys, int index, JSONObject json) throws JSONException {
		if (index < keys.length -1 && keys.length > 1) {
			JSONObject child = json.getJSONObject(keys[index]);
			index++;
			return getValueForKey(keys, index, child);
		}
		
		if (json.isNull(keys[index])) {
			return null;
		}
		return new NestedJsonAndKey(json, keys[index]);
	}
	
	
	private void setValueForKey(String key, JSONObject json, Object value) throws Exception {
		try {
			String[] keys = key.split("\\.");
			NestedJsonAndKey found = getValueForKey(keys, 0, json);
			if (found != null) {
				found.set(value);
			}
			
		} catch (Exception e) {
			Log.w(App.LOG_TAG, "Could not set value key: " + key + " on plot/tree object");
			throw e;
		}		
	}

	public void update(Model model) throws Exception {
		// If there is no valueView, this field was not rendered for edit
		if (this.valueView != null) {
			Object currentValue = getEditedValue();
			setValueForKey(this.key, model.getData(), currentValue);
		}
	}

	private Object getEditedValue() throws Exception {
		if (this.valueView != null) {
			
			if (this.valueView instanceof EditText) {
				return ((EditText) valueView).getText();
			} else if (this.valueView instanceof Button) {
				return this.valueView.getTag(R.id.choice_button_value_tag);
			} else {
				throw new Exception("Unknown ValueView type for field editing");
			}
		} 
		return null;
		
	}
}
