package org.azavea.otm.ui;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.azavea.otm.App;
import org.azavea.otm.InstanceInfo;
import org.azavea.otm.R;
import org.azavea.otm.adapters.LinkedHashMapAdapter;
import org.azavea.otm.rest.RequestGenerator;
import org.json.JSONArray;

import java.util.LinkedHashMap;
import java.util.List;

public class PublicInstanceListDisplay extends FilterableListDisplay<InstanceInfo> {

    @Override
    protected int getFilterHintTextId() {
        return R.string.filter_instances_hint;
    }

    @Override
    public void onCreate(Bundle data) {
        super.onCreate(data);

        final RequestGenerator rg = new RequestGenerator();
        final ProgressDialog loadingInstances = ProgressDialog.show(this,
                getString(R.string.instance_switcher_dialog_heading),
                getString(R.string.instance_switcher_loading_instances));

        rg.getPublicInstances(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONArray response) {
                loadingInstances.dismiss();

                List<InstanceInfo> list = InstanceInfo.getInstanceInfosFromJSON(response);
                InstanceInfo[] instances = list.toArray(new InstanceInfo[list.size()]);

                LinkedHashMap<CharSequence, List<InstanceInfo>> sectionedInstances =
                        groupListByKeyFirstLetter(instances, InstanceInfo::getName);
                LinkedHashMapAdapter<InstanceInfo> adapter = new LinkedHashMapAdapter<>(
                        PublicInstanceListDisplay.this, sectionedInstances,
                        R.layout.list_separator_row, R.id.separator,
                        R.layout.public_instance_element_row, R.id.text);
                renderList(adapter);
            }

            @Override
            public void onFailure(Throwable e, JSONArray errorResponse) {
                loadingInstances.dismiss();

                Log.e(App.LOG_TAG, "Error retrieving public instances", e);
                Toast.makeText(App.getAppInstance(), R.string.instances_failed,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void handleFailureMessage(Throwable e, String responseBody) {
                loadingInstances.dismiss();

                Log.e(App.LOG_TAG, "Error retrieving public instances", e);
                Toast.makeText(App.getAppInstance(), R.string.instances_failed,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
