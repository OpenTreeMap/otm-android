package org.azavea.otm.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.azavea.otm.App;
import org.azavea.otm.InstanceInfo;
import org.azavea.otm.LoginManager;
import org.azavea.otm.R;
import org.azavea.otm.data.Species;
import org.azavea.otm.rest.RequestGenerator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.azavea.otm.adapters.InstanceInfoArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;


public class InstanceSwitcherActivity extends Activity {
    private static final int REQUEST_CODE = 1;

    private Location userLocation;

    private ProgressDialog loadingInstances;
    private ProgressDialog loadingInstance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instance_switcher_activity);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loadingInstances != null) {
            loadingInstances.dismiss();
        }
        if (loadingInstance != null) {
            loadingInstance.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (App.hasInstanceCode()) {
            super.onBackPressed();
        }
    }

    private class InstanceListHandler extends JsonHttpResponseHandler {
        @Override
        public void onSuccess(JSONObject data) {
            final LinkedHashMap<CharSequence, List<InstanceInfo>> instances = new LinkedHashMap<>();
            instances.put("My Tree Maps", inflateForKey(data, "personal"));
            instances.put("Nearby Tree Maps", inflateForKey(data, "nearby"));

            final ListView instancesView = (ListView) findViewById(R.id.instance_list);
            InstanceInfoArrayAdapter adapter = new InstanceInfoArrayAdapter(instances, InstanceSwitcherActivity.this, userLocation);
            instancesView.setAdapter(adapter);
            instancesView.setEmptyView(findViewById(R.id.instance_list_empty));

            if (loadingInstances != null) {
                loadingInstances.dismiss();
            }

            instancesView.setOnItemClickListener((parent, v, position, id) -> {
                InstanceInfo instance = adapter.getItem(position).value;
                redirectToTabLayout(instance);
            });
        }
    }

    private void redirectToTabLayout(InstanceInfo instance) {
        loadingInstance = ProgressDialog.show(this,
                getString(R.string.instance_switcher_dialog_heading),
                getString(R.string.instance_switcher_loading_instance));
        String instanceCode = instance.getUrlName();
        App.reloadInstanceInfo(instanceCode, new RedirectCallback());
    }

    private class RedirectCallback implements Callback {
        @Override
        public boolean handleMessage(Message msg) {
            if (loadingInstance != null) {
                loadingInstance.dismiss();
            }

            Intent intent = new Intent(InstanceSwitcherActivity.this, TabLayout.class);
            InstanceSwitcherActivity.this.startActivity(intent);
            InstanceSwitcherActivity.this.finish();
            return true;
        }
    }

    // TODO: backport this algorithm, it's more fault tolerant
    private static Location getBestLocation(Criteria accuracyCrit) {
        Location location = null;
        Context context = App.getAppInstance();
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            final String bestProvider = locationManager.getBestProvider(accuracyCrit, true);
            List<String> providers = locationManager.getProviders(accuracyCrit, true);

            if (bestProvider != null) {
                Collections.sort(providers, (s1, s2) -> s1.equals(bestProvider) ? 1 :
                        s2.equals(bestProvider) ? -1 : 0);
            }

            for (String provider : providers) {
                location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    break;
                }
            }
        }

        return location;
    }

    @Override
    public void onStart() {
        super.onStart();

        // setup instance lists
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        userLocation = getBestLocation(criteria);
        RequestGenerator rg = new RequestGenerator();

        loadingInstances = ProgressDialog.show(this, getString(R.string.instance_switcher_dialog_heading), getString(R.string.instance_switcher_loading_instances));
        rg.getInstancesNearLocation(userLocation.getLatitude(),
                userLocation.getLongitude(),
                new InstanceListHandler());

        updateAccountElements();

        findViewById(R.id.login_button).setOnClickListener(v -> startActivity(new Intent(InstanceSwitcherActivity.this, LoginActivity.class)));
        findViewById(R.id.logout_button).setOnClickListener(v -> {
            App.getLoginManager().logOut(InstanceSwitcherActivity.this);
            // TODO: this might not be right anymore.
            // remove after configuring login manager to force instance switcher
            startActivity(new Intent(InstanceSwitcherActivity.this, LoginActivity.class));
        });
        findViewById(R.id.public_instances_button).setOnClickListener(v ->
                startActivityForResult(new Intent(this, PublicInstanceListDisplay.class), REQUEST_CODE));
    }

    private SpannableString makeUserNameString(String userName) {
        String prefix = "Not ";
        String suffix = "?";
        SpannableString content = new SpannableString(prefix + userName + suffix);
        content.setSpan(new UnderlineSpan(), prefix.length(), prefix.length() + userName.length(), 0);
        return content;
    }

    private void updateAccountElements() {
        TextView userNameView = (TextView) findViewById(R.id.username_text);
        LoginManager loginManager = App.getLoginManager();

        if (loginManager.isLoggedIn()) {
            findViewById(R.id.have_account_text).setVisibility(View.GONE);
            findViewById(R.id.login_button).setVisibility(View.GONE);
            try {
                userNameView.setText(makeUserNameString(loginManager.loggedInUser.getUserName()));
                userNameView.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                Log.e(App.LOG_TAG, "Could not get username.", e);
            }
            findViewById(R.id.logout_button).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.have_account_text).setVisibility(View.VISIBLE);
            findViewById(R.id.login_button).setVisibility(View.VISIBLE);
            userNameView.setVisibility(View.GONE);
            findViewById(R.id.logout_button).setVisibility(View.GONE);
        }
    }

    public ArrayList<InstanceInfo> inflateForKey(JSONObject data, String key) {
        JSONArray instances = data.optJSONArray(key);
        return InstanceInfo.getInstanceInfosFromJSON(instances);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (REQUEST_CODE): {
                if (resultCode == Activity.RESULT_OK) {
                    CharSequence instanceJSON = data.getCharSequenceExtra(PublicInstanceListDisplay.MODEL_DATA);
                    if (instanceJSON != null) {
                        try {
                            InstanceInfo instance = new InstanceInfo();
                            instance.setData(new JSONObject(instanceJSON.toString()));

                            redirectToTabLayout(instance);
                        } catch (JSONException e) {
                            String msg = "Unable to retrieve selected Tree Map";
                            Log.e(App.LOG_TAG, msg, e);
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                break;
            }
        }
    }
}
