package org.azavea.otm.ui;

import android.content.Intent;
import android.os.Bundle;

import org.azavea.helpers.Logger;
import org.azavea.otm.data.UDFCollectionDefinition;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class UDFCollectionEditActivity extends UDFCollectionActivity {

    public static final String INITIAL_VALUE = "value";
    public static final String TAG = "__collection_udf_identity_tag__";

    private JSONObject initialValue;
    private int tag;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        String json = getIntent().getStringExtra(INITIAL_VALUE);
        tag = getIntent().getIntExtra(TAG, -1);
        try {
            initialValue = new JSONObject(json);
            value = initialValue;
        } catch (JSONException e) {
            Logger.error("Could not parse passed in JSON string");
        }
    }

    @Override
    public List<JSONObject> getFieldTypes(UDFCollectionDefinition udfDef) {
        return udfDef.getTypesForEdit();
    }

    @Override
    protected void setUDF(UDFCollectionDefinition udfDef) {
        super.setUDF(udfDef);
        this.value = initialValue;
    }

    @Override
    protected void addArgumentsToResult(Intent result) {
        super.addArgumentsToResult(result);
        result.putExtra(TAG, tag);
    }
}
