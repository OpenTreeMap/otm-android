package org.azavea.otm;


public class Choice {
	private Object value;
	private String text;
	
	public Choice(String display, Object value) {
	    this.value = value;
	    text = display;
	}
	
	public Object getId() {
		return value;
	}
	
	public String getText() {
		return text;
	}
}
