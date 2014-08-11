package org.azavea.helpers;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;

// TODO: Should this class be a UDFDefinition Model subclass?
public class UDFCollectionHelper {

    public static final String DATA_TYPE = "data_type";
    public static final String FIELD_NAME = "name";
    public static final String COLLECTION_KEY = "field_key";

    public static LinkedHashMap<String, JSONObject> groupTypesByName(JSONObject udfDefinition) {
        // It is easier to work with the udf fields by name, so we pull it into a LinkedHashMap
        LinkedHashMap<String, JSONObject> data = new LinkedHashMap<>();
        JSONArray dataTypes = udfDefinition.optJSONArray(DATA_TYPE);
        for (int i = 0; i < dataTypes.length(); i++) {
            JSONObject dataType = dataTypes.optJSONObject(i);
            data.put(dataType.optString(FIELD_NAME), dataType);
        }
        return data;
    }

    public static List<String> getFieldNamesForUDF(JSONObject udfDefinition) {
        JSONArray dataTypes = udfDefinition.optJSONArray(DATA_TYPE);
        return JSONHelper.jsonStringArrayToList(dataTypes);
    }

    public static String getLabel(JSONObject udfDefinition) {
        String collectionKey = udfDefinition.optString(COLLECTION_KEY);
        App app = App.getAppInstance();
        if (collectionKey.contains(".") && "tree".equals(collectionKey.split("[.]")[0])) {
            return app.getString(R.string.tree);
        } else {
            return app.getString(R.string.planting_site);
        }
    }
}
