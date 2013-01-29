package org.azavea.otm.ui;

import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.PendingEdit;
import org.azavea.otm.data.PendingEditDescription;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.User;
import org.azavea.otm.data.UserType;
import org.azavea.otm.rest.RequestGenerator;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import org.azavea.otm.Field;

public class PendingItemDisplay extends Activity {
	Plot plot;
	String key;
	String label;
	CheckBox selectedValue;
	CheckBox currentValue;
	Vector<CheckBox> allPending = new Vector();
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_item);
        
        this.key = getIntent().getStringExtra("key");
        this.label = getIntent().getStringExtra("label");
       
        plot = new Plot();
		try {
			plot.setData(new JSONObject(getIntent().getStringExtra("plot")));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			render();
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, "Error rendering pending edit.", Toast.LENGTH_SHORT);
		}
		
		Log.d("PENDING", "key: " + key);
		Log.d("PENDING", "label: " + label);
		
		
		
    }
	
	public boolean canApprovePendingEdits() {
		User u = App.getLoginManager().loggedInUser;
		try {
			if (u != null) {
				UserType t = u.getUserType();
				int userLevel = t.getLevel();
				if (userLevel > User.ADMINISTRATOR_LEVEL) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	private void renderCurrentValue() {
		View row = getLayoutInflater().inflate(R.layout.pending_edit_row, null);
		ViewGroup container = (ViewGroup) findViewById(R.id.currentValue);
		Object value = null;
		boolean CURRENT_ONLY = true;
		try {
			value = plot.get(CURRENT_ONLY, key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String v;
		if (value != null  && !value.equals("")) {
			v = value.toString();
			container.addView(row);
		} else {
			v = "No current value";
		}
		((TextView)row.findViewById(R.id.value)).setText(v);
		((TextView)row.findViewById(R.id.user_name)).setText("");
		((TextView)row.findViewById(R.id.date)).setText("");
		
	}
	
	private void renderPendingValues() throws JSONException {
		ViewGroup container = (ViewGroup) findViewById(R.id.pendingEdits);
		PendingEditDescription pendingEditDescription = plot.getPendingEditForKey(key);
		List<PendingEdit> pendingEdits = pendingEditDescription.getPendingEdits();
		for (PendingEdit pendingEdit : pendingEdits) {	
			View pendingRow = getLayoutInflater().inflate(R.layout.pending_edit_row, null);
			String value = pendingEdit.getValue();
			String username = pendingEdit.getUsername();
			String date = "";
			try {
				date = pendingEdit.getSubmittedTime().toLocaleString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			int id = pendingEdit.getId();
						
			((TextView)pendingRow.findViewById(R.id.value)).setText(value);
			((TextView)pendingRow.findViewById(R.id.date)).setText(date);
			((TextView)pendingRow.findViewById(R.id.user_name)).setText(username);
			
			container.addView(pendingRow);
		}
	}
	
	private void renderTitle() {
		((TextView)findViewById(R.id.pending_edit_label)).setText(label);
	}
	
	
	public void render() throws JSONException {
		renderTitle();
		renderCurrentValue();
		renderPendingValues();
	}

	
	/*
	private OnClickListener createCheckboxClickListener(final CheckBox c) {
		return new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				selectedValue = c; 
				for (CheckBox cc : allPending) {
					cc.setChecked(c == cc);
				}
			}			
		};
	}
	
	public void handleSaveClick(View view) {
		if (selectedValue == null) {
			backToTreeList();
		} else if (selectedValue == currentValue) {
			rejectAll();
		} else {
			accept(selectedValue);
		}
		//TODO...
		//We are going to navigate to another activity here.. Is there a problem
		//if the REST requests from above haven't resolved yet?
		backToTreeList();
	}
	
	private void accept(CheckBox c) {
		RequestGenerator rc = new RequestGenerator();
		if (c.getTag() != null) {
			//rc.acceptPendingEdit(c.getTag(), pendingEditAccepted	);
		}
	}
	
	private void rejectAll() {
		RequestGenerator rc = new RequestGenerator();
	}
	
	private void backToTreeList() {
		Intent treeList = new Intent();
		// TODO put plot..
		startActivity(treeList);
	}
	
	private JsonHttpResponseHandler createRejectionResponseHandlder(final String key,JSONObject plotData) {
		Plot = new Plot();
		plot.setData(plotData);
		PendingEditDescription pd = plot.getPendingEditForKey(key);
		if (pd == null) {
			return new JsonHttpResponseHandler() {
				@Override
				public void onSuccess(JSONObject plotData) {
					Intent treeInfoIntent = new Intent(TreeInfoDisplay.class);
					treeInfoIntent.putExtra("plot", plotData.toString());
					startActivity(treeInfoIntent);
				};
				@Override
				protected void handleFailureMessage(Throwable arg0, String arg1) {
					Log.e("PENDING", arg0.toString());
					Log.e("PENDING", arg1);
					Toast.makeText(PendingItemDisplay.this, "Error with pending edits", Toast.LENGTH_SHORT);
				};
		} else {
			return new JsonHttpResponseHandler() {
				public void onSuccess(org.json.JSONArray plotData) {
					return createRejectionResponseHandler(key, plotData.toString());
				};
				protected void handleFailureMessage(Throwable arg0, String arg1) {
					Log.e("PENDING", arg0.toString());
					Log.e("PENDING", arg1);
					Toast.makeText(PendingItemDisplay.this, "Error with pending edits", Toast.LENGTH_SHORT);
				};
			}
		}
	}
	*/
}
