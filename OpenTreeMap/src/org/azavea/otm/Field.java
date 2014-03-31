package org.azavea.otm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.azavea.otm.data.Model;
import org.azavea.otm.data.PendingEdit;
import org.azavea.otm.data.PendingEditDescription;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.Species;
import org.azavea.otm.data.User;
import org.azavea.otm.ui.PendingItemDisplay;
import org.azavea.otm.ui.TreeInfoDisplay;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Field {
	private static final String TREE_SPECIES = "tree.species";

	// Any choices associated with this field, keyed by value
	private Map<Integer,Choice> choiceMap;
	
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
	 * Does the current user have permission to edit?
	 */
	public boolean canEdit;
	
	/**
	 * The text to append to the value as a unit
	 */
	public String unitText;

	/**
	 * Number of significant digits to round to
	 */
	public int digits;
	
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
	 * List of key/val pairs of choice options for this field 
	 */
	public JSONArray choicesDef = null;
	/**
	 *  Refers to a key in the json which determines this field.  Currently
	 *  it is only setup to support species list picker
	 */
	public String owner = null;
	
	public String infoUrl = null;
	
	public boolean editViewOnly = false;
	
	protected Field(String key, String label, boolean canEdit, String keyboard, 
			String format, String type, JSONArray choices, String owner, 
			String infoUrl,	boolean editViewOnly, String units, int digits) {
		this.key = key;
		this.label = label;
		this.canEdit = canEdit;
		this.keyboardResource = keyboard;
		this.format = format;
		this.type = type;
		this.owner = owner;
		this.infoUrl = infoUrl;
		this.editViewOnly = editViewOnly;
		this.unitText = units;
		this.digits = digits;
	}
	
	public static Field makeField(JSONObject fieldDef) {

		String key = fieldDef.optString("field_key");
		String label = fieldDef.optString("display_name");
		boolean canEdit = fieldDef.optBoolean("can_write");
		String format = fieldDef.optString("data_type");
		String keyboardResource = format == "double" ? "numberDecimal" : "text";
		JSONArray choices = fieldDef.optJSONArray("choices");
		String units = fieldDef.optString("units");
		int digits = fieldDef.optInt("digits");
		
		// Eco?
		String type = "";
		
		String owner = "";

		// tree.species gets special rendering rules
		if (key.equals(TREE_SPECIES)) {
		    owner = TREE_SPECIES;
		    format = "string";
		}
		
		// NOTE: Not enabled for OTM2 yet
		String infoUrl = fieldDef.optString("info_url");
		boolean editViewOnly = false;

		if (type != null && type.equals("eco")) {
			return new EcoField(key, label, canEdit, keyboardResource, format, type);
		} else {
			return new Field(key, label, canEdit, keyboardResource, format, 
					type, choices, owner, infoUrl, editViewOnly, units, digits);
		}
	}
	
	/* 
	 * Render a view to display the given model field in view mode
	 */	
	public View renderForDisplay(LayoutInflater layout, Plot model, Context context) 
	        throws JSONException {
		loadChoices();
		
		// our ui elements
		View container = layout.inflate(R.layout.plot_field_row, null);
        TextView label = (TextView)container.findViewById(R.id.field_label);
        TextView fieldValue = (TextView)container.findViewById(R.id.field_value);
    	View infoButton = container.findViewById(R.id.info);
    	View pendingButton = container.findViewById(R.id.pending);
        
    	if (this.owner != null && this.owner.equals(TREE_SPECIES)) {
    	    return renderSpeciesFields(layout, model, context, container,
                    label, fieldValue);
    	}

    	//set the label (simple)
    	label.setText(this.label);

    	// is this field pending (based on its own notion of pending or its owners.)
    	Boolean pending = (this.owner == null) ? 
    			isKeyPending(this.key, model) :
    			isKeyPending(this.owner, model);
    	
    	// Determine the current value of the field and update the ui. (Based on current
    	// value, value of simple pending edit, or value of pending edit where we have
    	// an owner field.
    	String value = null;
		if (!pending || this.owner == null) {
			 value = formatUnit(getValueForKey(this.key, model));
		} else {
			value = getValueForLatestPendingEditByRelatedField(this.key, this.owner, model); 
		}
    	fieldValue.setText(value);
    	
    	
    	// If the key is pending, display the arrow UI, and set up its click handler
    	//
    	// Note that the semantics of the bindPendingEditClickHandler function take
    	// a key into the pending edit array, and an optional related field.
    	//
    	// Where owner is defined, the owner is what we want to use to look up pending edits (and this.key
    	// becomes the related field.
        if (pending) {
        	if (this.owner!=null) {
        		bindPendingEditClickHandler(pendingButton, this.owner, this.key, model, context);
        	} else {
        		bindPendingEditClickHandler(pendingButton, this.key, null, model, context);
        	}
        	pendingButton.setVisibility(View.VISIBLE);
        }
        
        // If the field has a URL attached to it as an info description (IE for pests)
        // display the link.
        if (!TextUtils.isEmpty(this.infoUrl)) {
        	infoButton.setVisibility(View.VISIBLE);
        	bindInfoButtonClickHandler(infoButton, this.infoUrl, context);
        }
        
        return container;
	}

    private View renderSpeciesFields(LayoutInflater layout, Plot model,
            Context context, View container, TextView label, TextView fieldValue)
            throws JSONException {

        // tree.species gets exploded to a double row with sci name and common name
        label.setText("Scientific Name");
        fieldValue.setText(formatUnit(model.getScienticName()));

        View containerCommon = layout.inflate(R.layout.plot_field_row, null);
        TextView labelCommon = (TextView)containerCommon.findViewById(R.id.field_label);
        TextView fieldValueCommon = (TextView)containerCommon.findViewById(R.id.field_value);
        
        labelCommon.setText("Common Name");
        fieldValueCommon.setText(formatUnit(model.getCommonName()));
        
        LinearLayout doubleRow = new LinearLayout(context);
        doubleRow.addView(container);
        doubleRow.addView(containerCommon);
        
        doubleRow.setOrientation(LinearLayout.VERTICAL);
        return doubleRow;
    }

	/* 
	 * Render a view to display the given model field in edit mode
	 */
	public View renderForEdit(LayoutInflater layout, Plot model, User user, Context context) 
			throws JSONException {
		
		View container = null;
		loadChoices();
		
		if (this.canEdit) {
			container = layout.inflate(R.layout.plot_field_edit_row, null);
			Object value = getValueForKey(this.key, model.getData());
			
	        ((TextView)container.findViewById(R.id.field_label)).setText(this.label);
	        EditText edit = (EditText)container.findViewById(R.id.field_value);
	        Button choiceButton = (Button)container.findViewById(R.id.choice_select);
	        
	        // Show the correct type of input for this field
	        if (this.choiceItems != null) {
	        	edit.setVisibility(View.GONE);
	        	choiceButton.setVisibility(View.VISIBLE);
	        	this.valueView = choiceButton;
	        	setupChoiceDisplay(choiceButton, value);
	        	
	        } else if (this.owner != null) {
	        	edit.setVisibility(View.GONE);
	        	choiceButton.setVisibility(View.VISIBLE);
	        	this.valueView = choiceButton;
	        	setupOwnedField(choiceButton, value, model);
	        	
	        } else {
	        	String safeValue = (value != null && !value.equals(null)) 
	        			? value.toString() : ""; 
	        	edit.setVisibility(View.VISIBLE);
	        	choiceButton.setVisibility(View.GONE);
		        edit.setText(safeValue);
	        	this.valueView = edit;
	        	
	        	if (this.keyboardResource != null) {
					setFieldKeyboard(edit); 
		        }
	        	
        		// Special case for tree diameter.  Make a synced circumference field
	        	if (this.key.equals("dynamic-circumference")) {
	        		container.setId(R.id.dynamic_circumference);
	        		
	        	} else if (this.key.equals("tree.dbh")) {
	        		container.setId(R.id.dynamic_dbh);
	        		
	        	}
	        }
		}
        
		return container;
	}

	private void setupOwnedField(Button choiceButton, Object value, Model model) {
		if (this.owner.equals(TREE_SPECIES)) {
			JSONObject json  = model.getData();
			Object speciesId = getValueForKey(this.owner, json);
			
			if (speciesId != null) {
				// Set the button text to the common and sci name, which should be there
				// if a species Id is set.  We can grab these straight from the tree object
				// since the species list may still be loading
				String sciName = (String)getValueForKey("tree.sci_name", json);
				String commonName = "";
				try {
					commonName = (String)getValueForKey("tree.species_name", json);
				} catch (Exception e) {
					// GL #337 not consistently getting back the same species_name data
					// for trees without species. seems like it can either be null or the
					//entire key not present.
					e.printStackTrace();
				}
				choiceButton.setText(commonName + "\n" + sciName);
			} else {
				choiceButton.setText(R.string.unspecified_field_value);
			}
		}
		
	}

	public boolean hasChoices() {
		if (this.choiceMap == null || this.choiceMap.size() ==0) {
			return false;
		} else {
			return true;
		}
	}

	private void loadChoices() {
//		Choices choices = App.getFieldManager().getChoicesByName(this.choiceName);
//		if (choices != null && !choices.equals(null)) {
//			this.choiceMap = choices.getChoices();
//			this.choiceItems = choices.getItems();
//			this.choiceValues = choices.getValues();
//		}
	}

	
	private void setupChoiceDisplay(final Button choiceButton, Object value) {
		
		choiceButton.setText(R.string.unspecified_field_value);
		final int v = (value == null || value.equals(null) || value.equals("")) 
				? -1 : Integer.parseInt(value.toString()); 
		
		if (value != null) {
			Choice currentChoice = choiceMap.get(v);
			if (currentChoice != null) {
				choiceButton.setText(currentChoice.getText());
			}
		}
		
    	choiceButton.setTag(R.id.choice_button_value_tag, value);

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

				if (currentValue != null && !currentValue.equals(null)) {
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
		if (value == null || value.equals(null) || value.equals("")) {
			return App.getAppInstance().getResources()
					.getString(R.string.unspecified_field_value);
		} 
		if (hasChoices()) {
			// If there are choices for this field, display the choice
			// text, not the value
			int v = Integer.parseInt(value.toString());
			Choice choice = this.choiceMap.get(v);
			if (choice != null) {
				return choice.getText();
			}
		} 
		
		if (format != null) {
		    if (format.equals("float")) {
		        return formatWithDigits(value, this.digits) + " " + this.unitText;
		    }
		} 
		return value + " " + this.unitText;
	}
	
	public String formatWithDigits(Object value, int digits) {
		try { // attempt to round 'value'
		    Double d = Double.parseDouble(value.toString());
			return String.format("%." + digits + "f", d);
		} catch (ClassCastException e) {
			return value.toString();
		}
	}
	
	public static Object getValueForKey(String key, Plot plot) throws JSONException {
		PendingEditDescription pending = plot.getPendingEditForKey(key);
		if (pending != null) {
			return plot.getPendingEditForKey(key).getLatestValue();
		} else {
			return getValueForKey(key, plot.getData());
		}
	}
	
	public static boolean isKeyPending(String key, Plot plot) throws JSONException {
		PendingEditDescription pending =  plot.getPendingEditForKey(key);
		if (pending == null) {
			return false; 
		} else {
			return true;
		}
	}
	
	/**
	 * Return the value of a key name, which can be nested using . notation.  If the key does not 
	 * exist or the value of the key, it will return a null value
	 * @throws JSONException 
	 */
	public static Object getValueForKey(String key, JSONObject json) {
		try {
			String[] keys = key.split("\\.");
			NestedJsonAndKey found = getValueForKey(keys, 0, json, false);
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
	private static NestedJsonAndKey getValueForKey(String[] keys, int index, 
			JSONObject json, boolean createNodeIfEmpty) throws JSONException {
		if (index < keys.length -1 && keys.length > 1) {
			JSONObject child;
			if (json.get(keys[index]).equals(null) && createNodeIfEmpty) {
				child = new JSONObject();
				json.put(keys[index], child);
			} else {
				child= json.getJSONObject(keys[index]);
			}
			
			index++;
			return getValueForKey(keys, index, child, createNodeIfEmpty);
		}
		
		// We care to distinguish between a null value and a missing key.
		if (json.has(keys[index])) {
			return new NestedJsonAndKey(json, keys[index]);	
		} else if (createNodeIfEmpty == true) {
			// Create an empty node for this key
			return new NestedJsonAndKey(json.put(keys[index], ""), keys[index]);
		} else {
			return null;
		}
		
	}
	
	
	private void setValueForKey(String key, JSONObject json, Object value) throws Exception {
		try {
			String[] keys = key.split("\\.");
			NestedJsonAndKey found = getValueForKey(keys, 0, json, true);
			if (found != null) {
				found.set(value);
			} else {
				Log.w(App.LOG_TAG, "Specified key does not exist, cannot set value: " + key);
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
			
			// If this field is owned by another field, save the current
			// value to the owner, not to the displayed field
			String updateKey = this.key;
			if (this.owner != null) {
				updateKey = this.owner;
			} 
			// If the model doesn't have they key, add it.  This creates
			// a tree when tree values are added to a plot with no tree
			Plot p = (Plot)model;
			if (updateKey.split("[.]")[0].equals("tree") 
					&& !p.hasTree()
					&& currentValue != null) {
				p.createTree();
			}

			setValueForKey(updateKey, model.getData(), currentValue);
		}
	}

	private Object getEditedValue() throws Exception {
		if (this.valueView != null) {
			// For proper JSON encoding of types, we'll use the keyboard type
			// to cast the edited value to the desired Java type.  Choice buttons
			// are assumed to always be int
			
			if (this.valueView instanceof EditText) {
				EditText text = (EditText)valueView;
				if (hasNoValue(text.getText().toString())) {
					return null;
				}
				
				int inputType = text.getInputType();
				
				if ((inputType & InputType.TYPE_CLASS_TEXT) == InputType.TYPE_CLASS_TEXT) {
					return text.getText().toString();
					
				} else if ((inputType & InputType.TYPE_NUMBER_FLAG_DECIMAL) == InputType.TYPE_NUMBER_FLAG_DECIMAL) {
					return Double.parseDouble(text.getText().toString());
					
				} else if ((inputType & InputType.TYPE_CLASS_NUMBER) == InputType.TYPE_CLASS_NUMBER) {
					return Integer.parseInt(text.getText().toString());
					
				}
				
				return text.getText().toString();
				
			} else if (this.valueView instanceof Button) {
				Object choiceVal = this.valueView.getTag(R.id.choice_button_value_tag);
				if (choiceVal != null && !choiceVal.equals(null) && !choiceVal.equals("")) {
					return Integer.parseInt(choiceVal.toString());
				}
				
			} else {
				throw new Exception("Unknown ValueView type for field editing");
			}
		} 
		return null;
		
	}

	/**
	 * Check if the value of an edit text has any kind of value
	 * 
	 */
	private boolean hasNoValue(String text) {
		return text.equals(null) || text == null || text.trim().equals("");
	}

	public void attachClickListener(OnClickListener speciesClickListener) {
		if (this.valueView != null) {
			this.valueView.setOnClickListener(speciesClickListener);
		}
	}

	/**
	 * Manual setting of the field value from an external client.  The only
	 * current use case for this is setting the species value on a species 
	 * selector from the calling activity.
	 */
	public void setValue(Object value) {
		if (this.owner != null && this.owner.equals(TREE_SPECIES)) {
			
			try {
				Species species = (Species)value;
				
				if (this.valueView != null) {
				
					Button speciesButton = (Button)this.valueView;
				
					if (species != null) {
						
						speciesButton.setTag(R.id.choice_button_value_tag, species.getId());
						String label = species.getCommonName() + "\n" + species.getScientificName();
						speciesButton.setText(label);
				
					}
				}
				
			} catch (JSONException e) {
				Log.e(App.LOG_TAG, "Unable to set new species on tree", e);
				Toast.makeText(App.getAppInstance(), "Unable to set new species on tree", 
						Toast.LENGTH_LONG).show();
				
			}
		}
		
	}
	
	
	
	/*
	 * 
	 * key : the index into the pending edit array (IE Species)
	 * related field: the value to return. (IE Species Name)
	 * 
	 * If related field is null, return the plain value for the field.
	 *  (Example, when key is DBH, we want the numeric value.)
	 */
	private void bindPendingEditClickHandler(View b, final String key, final String relatedField, final Plot model, final Context context ) {
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// initialize the intent, and load it with some initial values
				Intent pendingItemDisplay = new Intent(context, PendingItemDisplay.class);
				pendingItemDisplay.putExtra("label" , label);
				pendingItemDisplay.putExtra("currentValue", formatUnit(getValueForKey(key, model.getData())));
				pendingItemDisplay.putExtra("key", key);
				
				// Now create an array of pending values, [{id: X, value: "42", username: "sam"}, ...]
				PendingEditDescription pendingEditDescription;
				try {
					pendingEditDescription = model.getPendingEditForKey(key);
					List<PendingEdit> pendingEdits = pendingEditDescription.getPendingEdits();
					JSONArray serializedPendingEdits = new JSONArray();
					for (PendingEdit pendingEdit : pendingEdits) {	
						// The value is the plain pending edit's value, or the value of the PE's 
						// related field.  (IE retrieve Species Name instead of a species ID.)
						String value;
						if (relatedField == null) {
							value = formatUnit(pendingEdit.getValue());
						} else {
							value = pendingEdit.getValue(relatedField);
						}
						
						//Continue on loading all of the pending edit data into the serializedPendingEdit
						// object
						JSONObject serializedPendingEdit = new JSONObject();
						serializedPendingEdit.put("id", pendingEdit.getId());
						serializedPendingEdit.put("value", value);
						serializedPendingEdit.put("username", pendingEdit.getUsername());
						try {
							serializedPendingEdit.put("date", pendingEdit.getSubmittedTime().toLocaleString());
						} catch (Exception e) {
							e.printStackTrace();
							serializedPendingEdit.put("date", "");
						}
						
						// and then append this edit onto the rest of them.
						serializedPendingEdits.put(serializedPendingEdit);
						
					}
					pendingItemDisplay.putExtra("pending", serializedPendingEdits.toString());
					
					// And start the target activity
					Activity a = (Activity)context;
					a.startActivityForResult(pendingItemDisplay, TreeInfoDisplay.EDIT_REQUEST);
				} catch (JSONException e1) {
					Toast.makeText(context, "Sorry, pending edits not available.", Toast.LENGTH_SHORT).show();
					e1.printStackTrace();
				}			
			}
		});
	}

	private void bindInfoButtonClickHandler(View infoButton, final String url, final Context context) {
		infoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url) );
				Activity a = (Activity)context;
				a.startActivity(browserIntent);
			}
		});
	}
	
	/*
	 * pending key: the key to get pending edits for.. IE tree.species
	 * related field: the key determining the representation to return.. IE tree.species_name
	 * plot: the plot object.
	 */
	
	//??REFACTOR: the signature suggests that this belongs on the Plot object.
	
	private static String getValueForLatestPendingEditByRelatedField(String relatedFieldKey, String pendingKey, Plot plot) {
		// get the pending edit description object for this plot
		PendingEditDescription ped;
		try {
			ped = plot.getPendingEditForKey(pendingKey);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		
		// get a list of pending edits
		List<PendingEdit> pendingEditList;
		try {
			pendingEditList = ped.getPendingEdits();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		
		// I assert that the most recent one is the first one. (argh.)
		PendingEdit mostRecentPendingEdit;
		if (pendingEditList.size() != 0) {
			mostRecentPendingEdit = pendingEditList.get(0);
		} else {
			return "";
		}
		
		// now give me the related field for that pending edit.
		String value =  mostRecentPendingEdit.getValue(relatedFieldKey);
		
		return value;

	}
}