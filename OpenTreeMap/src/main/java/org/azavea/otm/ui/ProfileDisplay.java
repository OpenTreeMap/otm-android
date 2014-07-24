package org.azavea.otm.ui;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.EditEntry;
import org.azavea.otm.data.EditEntryContainer;
import org.azavea.otm.data.User;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.ContainerRestHandler;
import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileDisplay extends Fragment {

    private static final int SHOW_LOGIN = 0;
    private static final int EDITS_TO_REQUEST = 5;
    private static LinkedHashMap<Integer, EditEntry> loadedEdits = new LinkedHashMap<>();
    // The fields on User which are displayed on Profile Page
    public static final String[][] userFields = {{"Username", "username"}, {"First Name", "first_name"},
            {"Last Name", "last_name"}, {"Organization", "organization"}};

    private final RequestGenerator client = new RequestGenerator();
    private int editRequestCount = 0;
    private boolean loadingRecentEdits = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.profile_activity, container, false);

        int switcherVisibility = App.hasSkinCode() ? View.GONE : View.VISIBLE;
        view.findViewById(R.id.change_instance_anonymous).setVisibility(switcherVisibility);
        view.findViewById(R.id.change_instance_loggedin).setVisibility(switcherVisibility);

        registerHandlers(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        editRequestCount = 0;
        loadProfile(getView(), getActivity().getLayoutInflater());
    }

    public void addMoreEdits() {
        if (!loadingRecentEdits) {
            Toast.makeText(App.getAppInstance(), "Loading more edits...", Toast.LENGTH_SHORT).show();
            renderRecentEdits(getActivity().getLayoutInflater());
        }

    }

    private void loadProfile(View view, LayoutInflater inflater) {
        if (App.getLoginManager().isLoggedIn()) {
            User user = App.getLoginManager().loggedInUser;
            renderUserFields(view, inflater, user, userFields);

            view.findViewById(R.id.profile_activity_loggedin).setVisibility(View.VISIBLE);
            view.findViewById(R.id.profile_activity_anonymous).setVisibility(View.GONE);
            /*
             * Presently, OTM2 is not loading User Edits.
             * 
             * NotifyingScrollView scroll =
             * (NotifyingScrollView)findViewById(R.id.userFieldsScroll);
             * scroll.setOnScrollToBottomListener(new
             * NotifyingScrollView.OnScrollToBottomListener() {
             * 
             * @Override public void OnScrollToBottom() { addMoreEdits(); } });
             */
        } else {
            view.findViewById(R.id.profile_activity_loggedin).setVisibility(View.GONE);
            view.findViewById(R.id.profile_activity_anonymous).setVisibility(View.VISIBLE);
        }
    }

    private void renderUserFields(View view, LayoutInflater inflater, User user, String[][] fieldNames) {
        LinearLayout fieldContainer = (LinearLayout) view.findViewById(R.id.profile_field_container);
        renderRecentEdits(inflater);

        fieldContainer.removeAllViews();
        for (String[] fieldPair : fieldNames) {
            String label = fieldPair[0];
            String value = user.getField(fieldPair[1]).toString();

            View row = inflater.inflate(R.layout.plot_field_row, null);
            ((TextView) row.findViewById(R.id.field_label)).setText(label);
            row.setTag(fieldPair[1]);
            ((TextView) row.findViewById(R.id.field_value)).setText(value);

            fieldContainer.addView(row);
        }
    }

    private void registerHandlers(final View view) {
        View.OnClickListener switchInstanceListener = v -> startActivity(new Intent(getActivity(), InstanceSwitcherActivity.class));
        view.findViewById(R.id.change_instance_anonymous).setOnClickListener(switchInstanceListener);
        view.findViewById(R.id.change_instance_loggedin).setOnClickListener(switchInstanceListener);

        view.findViewById(R.id.logout).setOnClickListener(v -> {
            App.getLoginManager().logOut(getActivity());
            loadProfile(view, getActivity().getLayoutInflater());
        });

        view.findViewById(R.id.change_password).setOnClickListener(v -> startActivity(new Intent(getActivity(), ChangePassword.class)));

        view.findViewById(R.id.change_profile_picture).setOnClickListener(v -> {
            // TODO: Refactor photo handling code in TreeEditDisplay for use here
        });

        view.findViewById(R.id.login).setOnClickListener(v -> {
            Intent login = new Intent(getActivity(), LoginActivity.class);
            startActivityForResult(login, SHOW_LOGIN);
        });
    }

    public void renderRecentEdits(final LayoutInflater layout) {

        // Presently, OTM2 edits are instance based and we are not loading
        // user recent edits for the profile page. I want to leave the
        // edits display code uncommented, so just return early for now
        boolean showEdits = false;
        if (!showEdits) {
            return;
        }

        // Don't load additional edits if there are edits currently loading
        if (loadingRecentEdits) {
            return;
        }

        loadingRecentEdits = true;
        try {
            client.getUserEdits(getActivity(), App.getLoginManager().loggedInUser, this.editRequestCount, this.EDITS_TO_REQUEST,
                    new ContainerRestHandler<EditEntryContainer>(new EditEntryContainer()) {

                        @Override
                        public void dataReceived(EditEntryContainer container) {
                            try {
                                addEditEntriesToView(layout, container);

                            } catch (JSONException e) {
                                Log.e(App.LOG_TAG, "Could not parse user edits response", e);
                                Toast.makeText(App.getAppInstance(), "Could not retrieve user edits",
                                        Toast.LENGTH_SHORT).show();
                            } finally {
                                loadingRecentEdits = false;
                            }
                        }

                        private void addEditEntriesToView(final LayoutInflater layout, EditEntryContainer container)
                                throws JSONException {

                            LinkedHashMap<Integer, EditEntry> edits = (LinkedHashMap<Integer, EditEntry>) container
                                    .getAll();
                            loadedEdits.putAll(edits);

                            LinearLayout scroll = (LinearLayout) getActivity().findViewById(R.id.user_edits);
                            for (EditEntry edit : edits.values()) {
                                // Create a view for this edit entry, and add a
                                // click handler to it
                                View row = layout.inflate(R.layout.recent_edit_row, null);

                                ((TextView) row.findViewById(R.id.edit_type)).setText(capitalize(edit.getName()));
                                String editTime = new SimpleDateFormat("MMMMM dd, yyyy 'at' h:mm a").format(edit
                                        .getEditTime());
                                ((TextView) row.findViewById(R.id.edit_time)).setText(editTime);
                                ((TextView) row.findViewById(R.id.edit_value)).setText("+"
                                        + Integer.toString(edit.getValue()));

                                row.setTag(edit.getId());

                                setPlotClickHandler(row);

                                scroll.addView(row);
                            }

                            // Increment the paging
                            editRequestCount += EDITS_TO_REQUEST;
                        }

                        private void setPlotClickHandler(View row) {
                            row.findViewById(R.id.edit_row).setOnClickListener(v -> {
                                try {
                                    // TODO: Login user check/prompt

                                    EditEntry edit = loadedEdits.get(v.getTag());
                                    if (edit.getPlot() != null) {
                                        final Intent viewPlot = new Intent(v.getContext(),
                                                TreeInfoDisplay.class);
                                        viewPlot.putExtra("plot", edit.getPlot().getData().toString());
                                        viewPlot.putExtra("user", App.getLoginManager().loggedInUser
                                                .getData().toString());
                                        startActivity(viewPlot);

                                    }
                                } catch (Exception e) {
                                    String msg = "Unable to display tree/plot info";
                                    Toast.makeText(v.getContext(), msg, Toast.LENGTH_SHORT).show();
                                    Log.e(App.LOG_TAG, msg, e);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Throwable e, String message) {
                            loadingRecentEdits = false;
                            Log.e(App.LOG_TAG, message);
                            Toast.makeText(App.getAppInstance(), "Could not retrieve user edits", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
            );

        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Failed to fetch user edits", e);
            Toast.makeText(getActivity(), "Could not retrieve user edits", Toast.LENGTH_SHORT).show();
        }
    }

    private String capitalize(String phrase) {
        String[] tokens = phrase.split("\\s");
        String capitalized = "";

        for (String token : tokens) {
            char capLetter = Character.toUpperCase(token.charAt(0));
            capitalized += " " + capLetter + token.substring(1, token.length());
        }
        return capitalized;
    }
}
