package org.azavea.otm.ui;

import org.azavea.helpers.Logger;
import org.azavea.otm.data.UDFCollectionDefinition;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;

public class UDFCollectionCreateActivity extends UDFCollectionActivity {

    @Override
    public List<JSONObject> getFieldTypes(UDFCollectionDefinition udfDef) {
        return udfDef.getTypesForAdd();
    }

    @Override
    protected void setUDF(UDFCollectionDefinition udfDef) {
        super.setUDF(udfDef);
        LinkedHashMap<String, JSONObject> fieldDataTypes = udfDef.groupTypesByName();
        for (JSONObject fieldType : fieldDataTypes.values()) {
            if (fieldType.has("default")) {
                try {
                    value.put(fieldType.optString("name"), fieldType.opt("default"));
                } catch (JSONException e) {
                    Logger.error("Unable to set default value on collection UDF", e);
                }
            }
        }
    }
}
