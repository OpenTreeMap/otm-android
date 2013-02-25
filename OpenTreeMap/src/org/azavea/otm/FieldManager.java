package org.azavea.otm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.Log;


public class FieldManager {
	private Context context;
	
	// All fields loaded from configuration file
	private ArrayList<FieldGroup> allFields = new ArrayList<FieldGroup>();
	private HashMap<String,Choices> allChoices = new HashMap<String,Choices>();
	
	
	public FieldManager(Context context) throws Exception {
		this.context = context;
		loadFieldDefinitions();
	}
	
	public Locale getLocale() {
		return new Locale(context.getString(R.string.iso_locale_language), 
				context.getString(R.string.iso_locale_country));
	}
	
	private void loadFieldDefinitions() throws Exception {
		// Load the field definitions from the included XML resource, and parse them 
		// into field objects
		InputStream filterFile = context.getResources().openRawResource(R.raw.configuration);
		try {
			DocumentBuilder xml = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xml.parse(filterFile);
			NodeList fields = doc.getElementsByTagName("fieldGroup");
			for (int i=0; i < fields.getLength(); i++) {
				Node fieldGroup = fields.item(i);
				
				Node name = fieldGroup.getAttributes().getNamedItem("name");
				String groupName = name == null ? null : name.getNodeValue();
				
				FieldGroup group = new FieldGroup(groupName);
				group.addFields(fieldGroup.getChildNodes());
				allFields.add(group);
			}
		} catch (Exception e) {
			throw new Exception("Invalid field xml file", e);
		}
	}

	private void loadChoiceDefinitions() throws Exception {
		// Load the field definitions from the included XML resource, and parse them 
		// into field objects
		InputStream configFile = context.getResources().openRawResource(R.raw.configuration);
		try {
			DocumentBuilder xml = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xml.parse(configFile);
			NodeList choices = doc.getElementsByTagName("choice");
			for (int i=0; i < choices.getLength(); i++) {
				Node choiceGroup = choices.item(i);
				
				String name = choiceGroup.getAttributes().getNamedItem("key").getNodeValue();
				String type = choiceGroup.getAttributes().getNamedItem("type").getNodeValue();
				
				Choices group = new Choices(name, type);
				group.addChoices(choiceGroup.getChildNodes());
				allChoices.put(name, group);
			}
		} catch (Exception e) {
			throw new Exception("Invalid field xml file", e);
		}
	}
	
	public Field getField(String name) {
		for (FieldGroup group : allFields) {
			for (Map.Entry<String,Field> field : group.getFields().entrySet()) {
				if (field.getKey().equals(name)) {
					return field.getValue();
				}
			}
		}
		return null;
	}
	
	public FieldGroup[] getFieldGroups() {
		return allFields.toArray(new FieldGroup[allFields.size()]);
	}
	
	public Choices getChoicesByName(String name) {
		if (allChoices.isEmpty()) {
			try {
				loadChoiceDefinitions();
			} catch (Exception e) {
				Log.e(App.LOG_TAG, "Unable to load choices xml file", e);
			}
		}
		if (allChoices.containsKey(name)) {
			return allChoices.get(name);
		}
		return null;
	}
}
	