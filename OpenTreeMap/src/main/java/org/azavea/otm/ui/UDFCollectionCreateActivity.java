package org.azavea.otm.ui;

import android.util.Log;

import org.azavea.otm.App;
import org.azavea.otm.data.UDFCollectionDefinition;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;

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
                    Log.e(App.LOG_TAG, "Unable to set default value on collection UDF", e);
                }
            }
        }
    }
}
