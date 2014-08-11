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

import org.azavea.otm.App;
import org.azavea.otm.Choice;
import org.azavea.otm.R;
import org.azavea.otm.data.UDFCollectionDefinition;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, UDFCollectionDefinition> udfDefinitions;
    private MenuItem nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_udf_add_activity);

        final List<UDFCollectionDefinition> udfDefinitions = getIntent().getExtras().getParcelableArrayList(UDF_DEFINITIONS);
        final ArrayList<Choice> choices = newArrayList(
                transform(udfDefinitions, u -> new Choice(u.getLabel(), u.getCollectionKey())));

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
            if (currentFragmentIndex + 1 < fieldFragments.size()) {
                setCurrentFragment(currentFragmentIndex + 1);
            } else {
                Intent result = new Intent();
                result.putExtra(collectionKey, value.toString());
                setResult(RESULT_OK, result);
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
        final UDFCollectionDefinition currentUDFDef = udfDefinitions.get(collectionKey);

        this.collectionKey = collectionKey;
        this.value = new JSONObject();
        this.fieldFragments = getFieldFragments(currentUDFDef);

        nextButton.setEnabled(true);
    }

    public void onValueChanged(String fieldName, String value) {
        try {
            this.value.put(fieldName, value);
            nextButton.setEnabled(true);
        } catch (JSONException e) {
            Toast.makeText(this, R.string.udf_create_error_saving, Toast.LENGTH_SHORT).show();
        }
    }

    private void setCurrentFragment(int fieldNumber) {
        this.currentFragmentIndex = fieldNumber;
        if (fieldNumber < 0) {
            setFragment(fieldChoiceFragment);
        } else if (fieldNumber < fieldFragments.size()) {
            setFragment(fieldFragments.get(fieldNumber));
            // If fieldNumber == fieldFragments.size() -1, change next button text?
            if (fieldNumber == fieldFragments.size() - 1) {
                nextButton.setTitle(R.string.udf_create_done_text);
            } else {
                nextButton.setTitle(R.string.udf_create_next_button_text);
            }
        } else {
            Log.e(App.LOG_TAG, "Fragment requested, but there was none to show...");
        }
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
        if (nextButton != null) {
            nextButton.setEnabled(false);
        }
    }

    private static LinkedHashMap<String, UDFCollectionDefinition> groupUdfDefinitionsByKey(Collection<UDFCollectionDefinition> udfDefs) {
        final LinkedHashMap<String, UDFCollectionDefinition> udfDefMap = new LinkedHashMap<>(udfDefs.size());
        for (UDFCollectionDefinition udfDef : udfDefs) {
            final String udfDefKey = udfDef.getCollectionKey();
            udfDefMap.put(udfDefKey, udfDef);
        }
        return udfDefMap;
    }

    private static List<Fragment> getFieldFragments(UDFCollectionDefinition udfDef) {
        final List<JSONObject> fieldDefs = newArrayList(udfDef.groupTypesByName().values());
        final List<Fragment> fragments = new ArrayList<>();
        for (JSONObject subFieldDef : fieldDefs) {
            final String type = subFieldDef.optString("type");

            if ("choice".equals(type)) {
                fragments.add(UDFSubFieldChoiceFragment.newInstance(subFieldDef));
            } else if ("date".equals(type)) {
                fragments.add(UDFDateFragment.newInstance(subFieldDef));
            } else {
                Log.w(App.LOG_TAG, "Unsupported Collection UDF field type " + type);
            }
        }
        return fragments;
    }
}
