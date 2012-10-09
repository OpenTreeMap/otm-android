package org.azavea.otm;

import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Choices {

	private String name;
	private Map<Integer,Choice> choices = new LinkedHashMap<Integer, Choice>();

	public Choices() {
		this.name = "";
	}
	
	public Choices(String name) {
		this.name = name;
	}
	
	public void addChoices(Map<Integer,Choice> choices) {
		this.choices = choices;
	}

	public void addChoices(NodeList choiceDefs) {
		if (choiceDefs != null) {
			for (int i=0; i < choiceDefs.getLength(); i++) {
				Node def = choiceDefs.item(i);
				if (def.getNodeType() == Node.ELEMENT_NODE) {
					addChoice(new Choice(def));
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

	public Map<Integer,Choice> getChoices() {
		return choices;
	}
}
