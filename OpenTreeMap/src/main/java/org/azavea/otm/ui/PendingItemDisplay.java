package org.azavea.otm.ui;

import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.apache.http.Header;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.PendingEditDescription;
import org.azavea.otm.data.Plot;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.LoggingJsonHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

public class PendingItemDisplay extends Activity {

    // We will need the key IE "tree.dbh" so that we can find the pending
    // edits in the plot
    String key;

    // We are going to manage the current and pending values
    // through the CheckBox objects that we instantiate for them.
    CheckBox selectedValue;
    CheckBox currentValue;
    Vector<CheckBox> allPending = new Vector<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_item);

        /*Render
            + label (IE "Diameter a Breast Height (in)")
            + current value (the value in the db that has been prev approved)
            + pending values
            + next/cancel buttons
        */
        String label = getIntent().getStringExtra("label");
        if (label != null && !label.equals("")) {
            renderTitle(label);
        }

        String current = getIntent().getStringExtra("current");
        renderCurrentValue(current);

        String pending = getIntent().getStringExtra("pending");
        if (pending != null && !pending.equals("")) {
            try {
                renderPendingValues(new JSONArray(getIntent().getStringExtra("pending")));
            } catch (JSONException e) {
                Toast.makeText(PendingItemDisplay.this, "Error rendering pending edits", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

        renderApprovalButtons();

    }

    public boolean canApprovePendingEdits() {
        if (App.getLoginManager().isLoggedIn()) {
            return App.getLoginManager().loggedInUser.isAdmin();
        } else {
            return false;
        }
    }


    private void renderCurrentValue(String value) {
        View row = getLayoutInflater().inflate(R.layout.pending_edit_row, null);
        ViewGroup container = (ViewGroup) findViewById(R.id.currentValue);
        currentValue = (CheckBox) row.findViewById(R.id.checkBox);

        ((TextView) row.findViewById(R.id.user_name)).setText("");
        ((TextView) row.findViewById(R.id.date)).setText("");

        if (value == null || value.equals("")) { // no current value.
            ((TextView) row.findViewById(R.id.value)).setText("No current value");
        } else {
            ((TextView) row.findViewById(R.id.value)).setText(value);
            if (canApprovePendingEdits()) {
                currentValue.setOnClickListener(checkBoxClickListener);
                currentValue.setVisibility(View.VISIBLE);
            }
        }
        container.addView(row);

    }

    private void renderPendingValues(JSONArray pendingEdits) {
        ViewGroup container = (ViewGroup) findViewById(R.id.pendingEdits);

        for (int i = 0; i < pendingEdits.length(); i++) {
            JSONObject pendingEdit;
            try {
                pendingEdit = pendingEdits.getJSONObject(i);
                String value = pendingEdit.getString("value");
                String username = pendingEdit.getString("username");
                String date = pendingEdit.getString("date");
                int id = pendingEdit.getInt("id");

                View pendingRow = getLayoutInflater().inflate(R.layout.pending_edit_row, null);

                ((TextView) pendingRow.findViewById(R.id.value)).setText(value);
                ((TextView) pendingRow.findViewById(R.id.date)).setText(date);
                ((TextView) pendingRow.findViewById(R.id.user_name)).setText(username);

                if (canApprovePendingEdits()) {
                    CheckBox cb = (CheckBox) pendingRow.findViewById(R.id.checkBox);
                    allPending.add(cb);
                    cb.setOnClickListener(checkBoxClickListener);
                    cb.setTag(id);
                    cb.setVisibility(View.VISIBLE);
                }
                container.addView(pendingRow);
            } catch (JSONException e) {
                Toast.makeText(PendingItemDisplay.this, "Error rendering pending edits", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private void renderTitle(String label) {
        ((TextView) findViewById(R.id.pending_edit_label)).setText(label);
    }

    private void renderApprovalButtons() {
        if (canApprovePendingEdits()) {
            findViewById(R.id.nav).setVisibility(View.VISIBLE);
        }
    }

    public OnClickListener checkBoxClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            selectedValue = (CheckBox) v;
            currentValue.setChecked(currentValue == v);
            for (CheckBox cc : allPending) {
                cc.setChecked(v == cc);
            }
        }
    };


    public void handleSaveClick(View view) {
        if (selectedValue == null) {
            Toast.makeText(PendingItemDisplay.this, "Please select an item.", Toast.LENGTH_SHORT).show();
        } else if (selectedValue == currentValue) {
            try {
                rejectAll();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "There was a problem saving the pending edits", Toast.LENGTH_LONG).show();
            }
        } else {
            approve(selectedValue);
        }
    }

    private void approve(CheckBox c) {
        RequestGenerator rc = new RequestGenerator();

        Object tag = c.getTag();
        if (tag != null) {
            Integer idToApprove = null;
            try {
                idToApprove = (Integer) tag;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (idToApprove != null) {
                try {
                    rc.approvePendingEdit(idToApprove.intValue(), handleApprovedPendingEdit);
                } catch (Exception e) {
                    Toast.makeText(PendingItemDisplay.this, "Error approving pending edit", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void rejectAll() throws JSONException {
        if (allPending.size() != 0) {
            int firstIdToReject = (Integer) allPending.get(0).getTag();

            RequestGenerator rc = new RequestGenerator();
            try {
                rc.rejectPendingEdit(firstIdToReject, createRejectionResponseHandlder(key));
            } catch (UnsupportedEncodingException e) {
                Toast.makeText(PendingItemDisplay.this, "Error rejecting all pending edits", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public JsonHttpResponseHandler createRejectionResponseHandlder(final String key) {
        return new LoggingJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject plotData) {
                try {
                    processNextId(plotData);
                } catch (JSONException e) {
                    doError();
                }
            }

            protected void processNextId(JSONObject plotData) throws JSONException {
                Plot plot = new Plot(plotData);
                PendingEditDescription ped = plot.getPendingEditForKey(key);
                if (ped == null) {
                    Intent intent = new Intent();
                    intent.putExtra("plot", plotData.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    int nextIdToReject = ped.getPendingEdits().get(0).getId();
                    RequestGenerator rc = new RequestGenerator();
                    try {
                        rc.rejectPendingEdit(nextIdToReject, createRejectionResponseHandlder(key));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        doError();
                    }
                }
            }

            @Override
            public void failure(Throwable arg0, String arg1) {
                doError();
            }

            protected void doError() {
                Toast.makeText(PendingItemDisplay.this, "Error with pending edits", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private final JsonHttpResponseHandler handleApprovedPendingEdit = new LoggingJsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject plotData) {
            Intent intent = new Intent();
            intent.putExtra("plot", plotData.toString());
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void failure(Throwable arg0, String arg1) {
            Toast.makeText(PendingItemDisplay.this, "Error with pending edits", Toast.LENGTH_SHORT).show();
        }
    };

    public void handleCancelClick(View view) {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

}
