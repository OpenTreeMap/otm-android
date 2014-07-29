package org.azavea.otm;


import java.io.Serializable;

public class Choice implements Serializable {
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
