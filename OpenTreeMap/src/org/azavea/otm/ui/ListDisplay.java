package org.azavea.otm.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.joelapenna.foursquared.widget.SegmentedButton;
import com.joelapenna.foursquared.widget.SegmentedButton.OnClickListenerSegmentedButton;

import org.azavea.lists.InfoList;
import org.azavea.lists.ListObserver;
import org.azavea.lists.NearbyList;
import org.azavea.lists.data.DisplayableModel;
import org.azavea.lists.data.DisplayablePlot;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.User;

import java.util.ArrayList;

public class ListDisplay extends Fragment implements ListObserver {
    private ListView listView;
    private InfoList infoList;
    private ProgressDialog dialog;
    private ArrayAdapter<DisplayableModel> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.list_activity, container, false);

        // Create the segmented buttons
        SegmentedButton buttons = (SegmentedButton) view.findViewById(R.id.segmented);
        buttons.clearButtons();

        ArrayList<String> buttonNames = new ArrayList<String>();
        buttonNames.add(getString(R.string.toggle_nearby));
        buttonNames.add(getString(R.string.toggle_recent));

        if (App.isPendingEnabled()) {
            buttonNames.add(1, getString(R.string.toggle_pending));
        }

        buttons.addButtons(buttonNames.toArray(new String[buttonNames.size()]));

        buttons.setOnClickListener(this::processRadioButtonSelection);

        return view;
    }

    @Override 
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden && infoList != null) {
            infoList.removeLocationUpdating();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        infoList.removeLocationUpdating();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        listView = (ListView) getActivity().findViewById(R.id.listItems);

        dialog = ProgressDialog.show(getActivity(), "", "Loading. Please wait...", true);

        infoList = App.getNearbyList(getActivity());
        infoList.addObserver(this);
        infoList.setupLocationUpdating(getActivity().getApplicationContext());
        listView.setOnItemClickListener(getOnClickListener());

        processRadioButtonSelection(0);

        update();
    }

    private void processRadioButtonSelection(int index) {
        dialog.show();
        NearbyList nearbyList = (NearbyList) infoList;
        switch (index) {
        case 0:
            nearbyList.setFilterRecent(false);
            nearbyList.setFilterPending(false);
            break;
        case 1:
            nearbyList.setFilterRecent(true);
            nearbyList.setFilterPending(false);
            break;
        case 2:
            nearbyList.setFilterRecent(false);
            nearbyList.setFilterPending(true);
            break;
        }
        nearbyList.update();
    }

    public ListView.OnItemClickListener getOnClickListener() {
        return (a, v, i, l) -> {
            Intent viewPlot = new Intent(getActivity(), TreeInfoDisplay.class);

            Plot selectedPlot = ((DisplayablePlot) a.getItemAtPosition(i)).getPlot();
            viewPlot.putExtra("plot", selectedPlot.getData().toString());

            User user = App.getLoginManager().loggedInUser;
            if (user != null) {
                viewPlot.putExtra("user", user.getData().toString());
            } else {
                // extra "user" will be null, which is handled in the
                // activity.
            }

            ListDisplay.this.startActivity(viewPlot);
        };
    }

    @Override
    public void update() {
        if (getActivity() != null) {
            adapter = new ArrayAdapter<DisplayableModel>(getActivity(), R.layout.simple_list_item, android.R.id.text1,
                    infoList.getDisplayValues());

            listView.setAdapter(adapter);

            adapter.notifyDataSetChanged();

            Log.d(App.LOG_TAG, "Hide dialog");
            dialog.hide();
        }
    }
}
