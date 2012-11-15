package org.azavea.otm.ui;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.EditEntry;
import org.azavea.otm.data.EditEntryContainer;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.User;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.ContainerRestHandler;
import org.azavea.otm.rest.handlers.RestHandler;
import org.azavea.views.NotifyingScrollView;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileDisplay extends Activity {

	private final RequestGenerator client = new RequestGenerator();
	private final int SHOW_LOGIN = 0;
	private final int EDITS_TO_REQUEST = 5;
	private final int PROFILE_PHOTO = 7;
	private int editRequestCount = 0;
	private boolean loadingRecentEdits = false; 
	private static LinkedHashMap<Integer, EditEntry> loadedEdits = new LinkedHashMap<Integer,EditEntry>();
	
	public String[][] userFields = { 
			{ "Username", "username" },
			{ "First Name", "firstname" }, 
			{ "Last Name", "lastname" },
			{ "Zip Code", "zipcode" }, 
			{ "Reputation", "reputation" } 
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadProfile();
	}

	@Override
	public void onResume() {
	    super.onResume();  
	    editRequestCount = 0;
	    loadProfile();
	}
	
	public void addMoreEdits() {
		if (!loadingRecentEdits) {
			Toast.makeText(App.getInstance(), "Loading more edits...", Toast.LENGTH_SHORT).show();
			renderRecentEdits(((Activity) this).getLayoutInflater());
		}
		
	}

	private void loadProfile() {
		if (App.getLoginManager().isLoggedIn()) {
			User user = App.getLoginManager().loggedInUser;
			setContentView(R.layout.profile_activity_loggedin);
			renderUserFields(user, userFields);
			
			NotifyingScrollView scroll = (NotifyingScrollView)findViewById(R.id.userFieldsScroll);
			scroll.setOnScrollToBottomListener(new NotifyingScrollView.OnScrollToBottomListener() {
				
				@Override
				public void OnScrollToBottom() {
					addMoreEdits();
				}
			});
		} else {
			setContentView(R.layout.profile_activity_anonymous);
		}
	}

	private void renderUserFields(User user, String[][] fieldNames) {
		LinearLayout scroll = (LinearLayout) this
				.findViewById(R.id.user_fields);
		LayoutInflater layout = ((Activity) this).getLayoutInflater();
		renderRecentEdits(layout);
		for (String[] fieldPair : fieldNames) {
			String label = fieldPair[0];
			String value = user.getField(fieldPair[1]).toString();
			
			View row = layout.inflate(R.layout.plot_field_row, null);
			((TextView) row.findViewById(R.id.field_label)).setText(label);
			((TextView) row.findViewById(R.id.field_value)).setText(value);

			scroll.addView(row);
		}
	}

	public void showLogin(View button) {
		Intent login = new Intent(this, LoginActivity.class);
		startActivityForResult(login, SHOW_LOGIN);
	}

	public void renderRecentEdits(final LayoutInflater layout) {
		// Don't load additional edits if there are edits currently loading
		if (loadingRecentEdits == true) {
			return;
		}
		
		loadingRecentEdits = true;
		try {
			client.getUserEdits(this, App.getLoginManager().loggedInUser, 
					this.editRequestCount, this.EDITS_TO_REQUEST, 
					new ContainerRestHandler<EditEntryContainer>(new EditEntryContainer()) {

						@Override
						public void dataReceived(EditEntryContainer container) {
							try {
								addEditEntriesToView(layout, container);
								
							} catch (JSONException e) {
								Log.e(App.LOG_TAG, "Could not parse user edits response", e);
								Toast.makeText(App.getInstance(), "Could not retrieve user edits", 
										Toast.LENGTH_SHORT).show();
							} finally {
								loadingRecentEdits = false;
							}
						}

						private void addEditEntriesToView(final LayoutInflater layout,
								EditEntryContainer container) throws JSONException {
							
							LinkedHashMap<Integer, EditEntry> edits = 
									(LinkedHashMap<Integer, EditEntry>) container.getAll();
							loadedEdits.putAll(edits);
							
							LinearLayout scroll = (LinearLayout) findViewById(R.id.user_edits);
							for (EditEntry edit: edits.values()) {
								// Create a view for this edit entry, and add a click handler to it
								View row = layout.inflate(R.layout.recent_edit_row, null);
								
								((TextView) row.findViewById(R.id.edit_type)).setText(capitalize(edit.getName()));
								String editTime = new SimpleDateFormat("MMMMM dd, yyyy 'at' h:mm a").format(edit.getEditTime());
								((TextView) row.findViewById(R.id.edit_time)).setText(editTime);
								((TextView) row.findViewById(R.id.edit_value)).setText("+" + Integer.toString(edit.getValue()));
								
								row.setTag(edit.getId());
								
								setPlotClickHandler(row);
								
								scroll.addView(row);
							}
							
							// Increment the paging
							editRequestCount += EDITS_TO_REQUEST;
						}

						private void setPlotClickHandler(View row) {
							
							((RelativeLayout) row.findViewById(R.id.edit_row)).setOnClickListener(new View.OnClickListener() {
								
								@Override
								public void onClick(View v) {
									try {
										// TODO: Login user check/prompt
										
										EditEntry edit = loadedEdits.get(v.getTag());
										if (edit.getPlot() != null) {
							    			final Intent viewPlot = new Intent(v.getContext(), TreeInfoDisplay.class);
							    			viewPlot.putExtra("plot", edit.getPlot().getData().toString());
							    			viewPlot.putExtra("user", App.getLoginManager().loggedInUser.getData().toString());
							    			startActivity(viewPlot);
							    			
										}
									} catch (Exception e) {
										String msg = "Unable to display tree/plot info";
										Toast.makeText(v.getContext(), msg, Toast.LENGTH_SHORT).show();
										Log.e(App.LOG_TAG, msg, e);
									} 
								}
							});
						}		
						
						@Override
						public void onFailure(Throwable e, String message) {
							loadingRecentEdits = false;
							Log.e(App.LOG_TAG, message);
							Toast.makeText(App.getInstance(), "Could not retrieve user edits", 
									Toast.LENGTH_SHORT).show();
						}
					});
			
		} catch (JSONException e) {
			Log.e(App.LOG_TAG, "Failed to fetch user edits", e);
			Toast.makeText(this, "Could not retrieve user edits", Toast.LENGTH_SHORT).show();
		}
	}

	public void logoutUser(View button) {
		App.getLoginManager().logOut();
		loadProfile();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Log.d(App.LOG_TAG, "Reload profile for new user login");
			if (requestCode == SHOW_LOGIN) {
				loadProfile();				
			} else if (requestCode == PROFILE_PHOTO) {
				Bitmap bm = (Bitmap) data.getExtras().get("data");
		  		RequestGenerator rc = new RequestGenerator();
				try {
					rc.addProfilePhoto(App.getInstance(), bm, addProfilePhotoHandler);
				} catch (JSONException e) {
					Log.e(App.LOG_TAG, "Error profile tree photo.", e);
				}
			}
		} else if (resultCode == RESULT_CANCELED) {
			// Nothing?
		}
	}
	
	private String capitalize(String phrase) {
		String[] tokens = phrase.split("\\s");
		String capitalized = "";

		for(int i = 0; i < tokens.length; i++){
		    char capLetter = Character.toUpperCase(tokens[i].charAt(0));
		    capitalized +=  " " + capLetter + tokens[i].substring(1, tokens[i].length());
		}
		return capitalized;
	}
	
	public void changeProfilePhoto(View view) {
		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, PROFILE_PHOTO);
	}
	
	
	//TODO There is a lot of debugging in this function for development purposes. remove.
	//TODO Possible to DRY this up WRT the same handler for tree photos?
	private JsonHttpResponseHandler addProfilePhotoHandler = new JsonHttpResponseHandler() {
		public void onSuccess(JSONObject response) {
			Log.d("AddProfilePhoto", "addTreePhotoHandler.onSuccess");
			Log.d("AddProfilePhoto", response.toString());
			try {
				if (response.get("status").equals("success")) {
					Toast.makeText(App.getInstance(), "The profile photo was added.", Toast.LENGTH_LONG).show();		
				} else {
					Toast.makeText(App.getInstance(), "Unable to add profile photo.", Toast.LENGTH_LONG).show();		
					Log.d("AddProfilePhoto", "photo response no success");
				}
			} catch (JSONException e) {
				Toast.makeText(App.getInstance(), "Unable to add profile photo", Toast.LENGTH_LONG).show();
			}
		};
		public void onFailure(Throwable e, JSONObject errorResponse) {
			Log.e("AddProfilePhoto", "addTreePhotoHandler.onFailure");
			Log.e("AddProfilePhoto", errorResponse.toString());
			Log.e("AddProfilePhoto", e.getMessage());
			Toast.makeText(App.getInstance(), "Unable to add profile photo.", Toast.LENGTH_LONG).show();		
		};
		
		protected void handleFailureMessage(Throwable e, String responseBody) {
			Log.e("addProfilePhoto", "addTreePhotoHandler.handleFailureMessage");
			Log.e("addProfilePhoto", "e.toString " + e.toString());
			Log.e("addProfilePhoto", "responseBody: " + responseBody);
			Log.e("addProfilePhoto", "e.getMessage: " + e.getMessage());
			Log.e("addProfilePhoto", "e.getCause: " + e.getCause());
			e.printStackTrace();
			Toast.makeText(App.getInstance(), "The profile photo was added.", Toast.LENGTH_LONG).show();					
		};
	};
}
