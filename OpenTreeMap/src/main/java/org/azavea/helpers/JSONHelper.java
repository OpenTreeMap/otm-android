package org.azavea.helpers;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class JSONHelper {
    public static List<String> jsonStringArrayToList(JSONArray array) {
        List<String> strings = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            strings.add(array.isNull(i) ? null : array.optString(i));
        }
        return strings;
    }
}
