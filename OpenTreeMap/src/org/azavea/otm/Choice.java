package org.azavea.otm;

import org.w3c.dom.Node;

public class Choice {
	private int id;
	private String text;
	
	public Choice(Node choiceDef) {
		id = Integer.parseInt(
				choiceDef.getAttributes().getNamedItem("value").getNodeValue());
		text = choiceDef.getTextContent() != null ? 
				choiceDef.getTextContent() : "<Unknown>";
	}
	
	public int getId() {
		return id;
	}
	
	public String getText() {
		return text;
	}
}
