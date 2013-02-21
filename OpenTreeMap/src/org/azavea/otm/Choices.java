package org.azavea.otm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Choices {

	private String name;
	private String valueType;
	
	private Map<Integer,Choice> choices = new LinkedHashMap<Integer, Choice>();
	private ArrayList<String> items = new ArrayList<String>();
	private ArrayList<Integer> values = new ArrayList<Integer>();
	
	public Choices() {
		this.name = "";
	}
	
	public Choices(String name, String valueType) {
		this.name = name;
		this.valueType = valueType;
	}
	
	public void addChoices(Map<Integer,Choice> choices) {
		this.choices = choices;
	}

	public void addChoices(NodeList choiceDefs) {
		if (choiceDefs != null) {
			for (int i=0; i < choiceDefs.getLength(); i++) {
				Node def = choiceDefs.item(i);
				if (def.getNodeType() == Node.ELEMENT_NODE) {
					Choice choice = new Choice(def);
					addChoice(choice);
					this.items.add(choice.getText());
					this.values.add(choice.getId());
				}
			}
		}
	}
	
	public void addChoice(Choice choice) {
		this.choices.put(choice.getId(), choice);
	}

	public String getName() {
		return name;
	}

	public String getValueType() {
		return this.valueType;
	}
	
	public Map<Integer,Choice> getChoices() {
		return choices;
	}

	public ArrayList<String> getItems() {
		return this.items;
	}

	public ArrayList<Integer> getValues() {
		return this.values;
	}
}
