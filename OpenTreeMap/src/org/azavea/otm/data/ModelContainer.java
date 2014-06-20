package org.azavea.otm.data;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

public abstract class ModelContainer<T> {
    protected JSONArray data;

    public void setData(JSONArray array) {
        this.data = array;
    }

    public JSONArray getData() {
        return data;
    }

    public abstract Map<Integer, T> getAll() throws JSONException;
}
