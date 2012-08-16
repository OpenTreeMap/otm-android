package org.azavea.otm;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;


public class FieldManager {
	private Context context;
	
	// All fields loaded from configuration file
	private  ArrayList<FieldGroup> allFields = new ArrayList<FieldGroup>();
	
	
	public FieldManager(Context context) throws Exception {
		this.context = context;
		loadFilterDefinitions();
	}
	
	private void loadFilterDefinitions() throws Exception {
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
	
	public FieldGroup[] getFieldGroups() {
		return allFields.toArray(new FieldGroup[allFields.size()]);
	}
	
}
	