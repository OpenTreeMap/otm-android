package org.azavea.otm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.azavea.helpers.UDFCollectionHelper;
import org.azavea.otm.App;
import org.azavea.otm.Choice;
import org.azavea.otm.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Collections2.transform;

public class UDFCollectionCreateActivity extends ActionBarActivity {

    public static final String UDF_DEFINITIONS = "udfs";

    private String collectionKey;
    private JSONObject value;
    private UDFKeyChoiceFragment fieldChoiceFragment;
    private List<Fragment> fieldFragments;
    // Index into subFieldDefinitions, set to -1 so the first +1 puts us at the start of fieldFragments
    private int currentFragmentIndex = -1;
    private Map<String, JSONObject> udfDefinitions;
    private MenuItem nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_udf_add_activity);

        final List<String> jsonUdfDefinitions = (ArrayList<String>) getIntent().getExtras().getSerializable(UDF_DEFINITIONS);
        final Collection<JSONObject> udfDefinitions = filter(transform(jsonUdfDefinitions, j -> {
            try {
                return new JSONObject(j);
            } catch (JSONException e) {
                Log.e(App.LOG_TAG, "Failed to parse passed in JSON String", e);
                return null;
            }
        }), u -> u != null);
        final ArrayList<Choice> choices = newArrayList(
                transform(udfDefinitions,
                          u -> new Choice(UDFCollectionHelper.getLabel(u),
                                          u.optString(UDFCollectionHelper.COLLECTION_KEY))));

        this.udfDefinitions = groupUdfDefinitionsByKey(udfDefinitions);

        fieldChoiceFragment = UDFKeyChoiceFragment.newInstance(choices);
        setFragment(fieldChoiceFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.udf_create_menu, menu);
        // We need a handle on the next button to enable/disable it
        nextButton = menu.findItem(R.id.udf_create_next_button);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.udf_create_next_button) {
            if (currentFragmentIndex < fieldFragments.size()) {
                setCurrentFragment(currentFragmentIndex + 1);
            } else {
                Intent result = new Intent();
                result.putExtra(collectionKey, value.toString());
                setResult(TreeEditDisplay.FIELD_ACTIVITY_REQUEST_CODE, result);
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (currentFragmentIndex < 0) {
            super.onBackPressed();
        } else {
            setCurrentFragment(currentFragmentIndex - 1);
        }
    }

    public void onUDFFieldSelected(String collectionKey) {
        final JSONObject currentUDFDef = udfDefinitions.get(collectionKey);

        this.collectionKey = collectionKey;
        this.fieldFragments = getFieldFragments(currentUDFDef);
        this.value = new JSONObject();

        nextButton.setEnabled(true);
    }

    public void onValueChanged(String fieldName, String value) {
        try {
            this.value.put(fieldName, value);
            nextButton.setEnabled(true);
        } catch (JSONException e) {
            Toast.makeText(this, "Something weird happened", Toast.LENGTH_SHORT).show();
        }
    }

    private void setCurrentFragment(int fieldNumber) {
        this.currentFragmentIndex = fieldNumber;
        if (fieldNumber < 0) {
            setFragment(fieldChoiceFragment);
        } else if (fieldNumber < fieldFragments.size()) {
            setFragment(fieldFragments.get(fieldNumber));
            // If fieldNumber == fieldFragments.size() -1, change next button text?
        } else {
            // TODO:, set the activity result, or WTF?  Unclear
        }
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
        // TODO: Change ActionBar title?
        if (nextButton != null) {
            nextButton.setEnabled(false);
        }
    }

    private static LinkedHashMap<String, JSONObject> groupUdfDefinitionsByKey(Collection<JSONObject> udfDefs) {
        final LinkedHashMap<String, JSONObject> udfDefMap = new LinkedHashMap<>(udfDefs.size());
        for (JSONObject udfDef : udfDefs) {
            if (!udfDef.isNull(UDFCollectionHelper.COLLECTION_KEY)) {
                final String udfDefKey = udfDef.optString(UDFCollectionHelper.COLLECTION_KEY);
                udfDefMap.put(udfDefKey, udfDef);
            }
        }
        return udfDefMap;
    }

    private static List<Fragment> getFieldFragments(JSONObject udfDef) {
        final List<JSONObject> fieldDefs = newArrayList(UDFCollectionHelper.groupTypesByName(udfDef).values());
        final List<Fragment> fragments = new ArrayList<>();
        for (JSONObject subFieldDef : fieldDefs) {
            final String type = subFieldDef.optString("type");

            if ("choice".equals(type)) {
                fragments.add(UDFSubFieldChoiceFragment.newInstance(subFieldDef));
            } else {
                // TODO: log
            }
        }
        return fragments;
    }
}
