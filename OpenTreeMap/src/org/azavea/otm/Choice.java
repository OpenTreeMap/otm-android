package org.azavea.otm;


public class Choice {
	private String value;
	private String text;
	
	public Choice(String display, String value) {
	    this.value = value;
	    text = display;
	}
	
	public String getValue() {
		return value;
	}
	
	public String getText() {
		return text;
	}
}
