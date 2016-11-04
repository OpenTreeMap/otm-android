package org.azavea.otm.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.azavea.helpers.Logger;
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

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;

public abstract class UDFCollectionActivity extends Activity {

    public static final String UDF_DEFINITIONS = "udfs";

    protected JSONObject value;

    private String collectionKey;
    private Fragment currentFragment = null;
    private UDFKeyChoiceFragment fieldChoiceFragment;
    private List<Fragment> fieldFragments;
    // Index into subFieldDefinitions, set to -1 so the first +1 puts us at the start of fieldFragments
    private int currentFragmentIndex = -1;
    private Map<String, UDFCollectionDefinition> udfDefinitions;
    private MenuItem nextButton;

    public abstract List<JSONObject> getFieldTypes(UDFCollectionDefinition udfDef);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_udf_add_activity);

        final List<UDFCollectionDefinition> udfDefinitions = getIntent().getParcelableArrayListExtra(UDF_DEFINITIONS);
        final ArrayList<Choice> choices = newArrayList(
                transform(udfDefinitions, u -> new Choice(u.getLabel(), u.getCollectionKey())));

        this.udfDefinitions = groupUdfDefinitionsByKey(udfDefinitions);

        // Only show the screen to select the UDF type if there is more than 1
        if (udfDefinitions.size() == 1) {
            setUDF(udfDefinitions.get(0));
            setCurrentFragment(0);
        } else {
            fieldChoiceFragment = UDFKeyChoiceFragment.newInstance(choices);
            setFieldChoiceFragment();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.udf_create_menu, menu);
        // We need a handle on the next button to enable/disable it
        nextButton = menu.findItem(R.id.udf_create_next_button);
        if (fieldFragments != null && fieldFragments.size() == 1) {
            nextButton.setTitle(R.string.udf_create_done_text);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.udf_create_next_button) {
            if (currentFragmentIndex + 1 < fieldFragments.size()) {
                setCurrentFragment(currentFragmentIndex + 1);
            } else {
                Intent result = new Intent();
                addArgumentsToResult(result);
                setResult(RESULT_OK, result);
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void addArgumentsToResult(Intent result) {
        result.putExtra(collectionKey, value.toString());
    }

    @Override
    public void onBackPressed() {
        if (currentFragmentIndex < 0) {
            super.onBackPressed();
        } else {
            setCurrentFragment(currentFragmentIndex - 1);
        }
    }

    /**
     * Set the currently selected collection UDF.
     * This will reset all stored state if a different collection UDF had already been selected
     *
     * @param collectionKey the key of the collection UDF
     */
    public void onUDFFieldSelected(String collectionKey) {
        if (this.collectionKey == null || !this.collectionKey.equals(collectionKey)) {
            final UDFCollectionDefinition newUDFDef = udfDefinitions.get(collectionKey);
            setUDF(newUDFDef);
        }

        nextButton.setEnabled(true);
    }

    protected void setUDF(UDFCollectionDefinition currentUDFDef) {
        this.collectionKey = currentUDFDef.getCollectionKey();
        this.value = new JSONObject();
        this.fieldFragments = getFieldFragments(currentUDFDef);
    }

    /**
     * Set the value of the specified UDF sub-field
     *
     * @param fieldName the field name to set a value for
     * @param value     the value to save
     */
    public void onValueChanged(String fieldName, String value) {
        try {
            this.value.put(fieldName, value);
            nextButton.setEnabled(true);
        } catch (JSONException e) {
            Logger.error(e);
            Toast.makeText(this, R.string.udf_create_error_saving, Toast.LENGTH_SHORT).show();
        }
    }

    public void setActionBarTitle(String title) {
        getActionBar().setTitle(title);
    }

    private void setCurrentFragment(int fieldNumber) {
        this.currentFragmentIndex = fieldNumber;
        if (fieldNumber < 0 && fieldChoiceFragment == null) {
            // If there is no UDF choice screen, go back to the tree edit screen
            super.onBackPressed();
        } else if (fieldNumber < 0) {
            setFieldChoiceFragment();

        } else if (fieldNumber < fieldFragments.size()) {
            setFragment(fieldFragments.get(fieldNumber));
            if (nextButton != null) {
                if (fieldNumber == fieldFragments.size() - 1) {
                    nextButton.setTitle(R.string.udf_create_done_text);
                } else {
                    nextButton.setTitle(R.string.udf_create_next_button_text);
                }
            }
        } else {
            Logger.error("Fragment requested, but there was none to show...");
        }
    }

    private void setFieldChoiceFragment() {
        // The collection UDFs in a group usually have the same name, so just use the last one
        final String title = getLast(udfDefinitions.values()).getDisplayName();
        setActionBarTitle(title);
        setFragment(fieldChoiceFragment);
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (currentFragment != null) {
            ft.hide(currentFragment);
        }
        currentFragment = fragment;
        if (fragment.isHidden()) {
            ft.show(fragment);
        } else {
            ft.add(R.id.fragment_container, fragment);
        }
        ft.commit();
        if (nextButton != null) {
            nextButton.setEnabled(false);
        }
    }

    private List<Fragment> getFieldFragments(UDFCollectionDefinition udfDef) {
        final List<JSONObject> fieldDefs = getFieldTypes(udfDef);
        final List<Fragment> fragments = new ArrayList<>();
        for (JSONObject subFieldDef : fieldDefs) {
            final String type = subFieldDef.optString("type");

            if ("choice".equals(type)) {
                fragments.add(UDFSubFieldChoiceFragment.newInstance(subFieldDef));
            } else if ("date".equals(type)) {
                fragments.add(UDFDateFragment.newInstance(subFieldDef));
            } else {
                Logger.warning("Unsupported Collection UDF field type " + type);
            }
        }
        return fragments;
    }

    private static LinkedHashMap<String, UDFCollectionDefinition> groupUdfDefinitionsByKey(Collection<UDFCollectionDefinition> udfDefs) {
        final LinkedHashMap<String, UDFCollectionDefinition> udfDefMap = new LinkedHashMap<>(udfDefs.size());
        for (UDFCollectionDefinition udfDef : udfDefs) {
            final String udfDefKey = udfDef.getCollectionKey();
            udfDefMap.put(udfDefKey, udfDef);
        }
        return udfDefMap;
    }
}
