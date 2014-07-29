package org.azavea.otm.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import org.azavea.helpers.UDFCollectionHelper;
import org.azavea.otm.App;
import org.azavea.otm.Choice;
import org.azavea.otm.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Collections2.transform;

public class UDFCollectionCreateActivity extends ActionBarActivity implements ChoiceFragment.Listener {

    public static final String UDF_DEFINITIONS = "udfs";

    private final JSONObject value = new JSONObject();

    private List<ChoiceFragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_udf_add_activity);

        List<String> jsonUdfDefinitions = (ArrayList<String>) getIntent().getExtras().getSerializable(UDF_DEFINITIONS);
        Collection<JSONObject> udfDefinitions = filter(transform(jsonUdfDefinitions, j -> {
            try {
                return new JSONObject(j);
            } catch (JSONException e) {
                Log.e(App.LOG_TAG, "Failed to parse passed in JSON String", e);
                return null;
            }
        }), u -> u != null);
        ArrayList<Choice> choices = newArrayList(
                transform(udfDefinitions,
                          u -> new Choice(UDFCollectionHelper.getLabel(u),
                                          u.optString(UDFCollectionHelper.COLLECTION_KEY))));

        ChoiceFragment firstFragment = ChoiceFragment.newInstance(choices);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, firstFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onChoiceSelected(String choiceValue) {
        Toast.makeText(this, choiceValue, Toast.LENGTH_SHORT).show();
    }
}
