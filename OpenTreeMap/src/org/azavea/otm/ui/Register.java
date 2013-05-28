package org.azavea.otm.ui;

import org.azavea.otm.App;
import org.azavea.otm.LoginManager;
import org.azavea.otm.R;
import org.azavea.otm.data.User;
import org.azavea.otm.rest.RequestGenerator;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Register extends PhotoActivity{
	private String username;
	private String password;	
	private Bitmap profilePicture;
	private LoginManager loginManager = App.getLoginManager();
	
	
	/*
	 * Activity overrides
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		//setDebuggingValues();
	}
	
	/*
	 * UI Event Handlers 
	 */
	public void handleRegisterClick(View view) {
		password 	= ((EditText)findViewById(R.id.register_password)).getText().toString();
		username 	= ((EditText)findViewById(R.id.register_username)).getText().toString();
		
		String email 		= ((EditText)findViewById(R.id.register_email)).getText().toString();
		String password2 	= ((EditText)findViewById(R.id.register_password2)).getText().toString();
		String firstName 	= ((EditText)findViewById(R.id.register_firstName)).getText().toString();
		String lastName 	= ((EditText)findViewById(R.id.register_lastName)).getText().toString();
		String zipCode 		= ((EditText)findViewById(R.id.register_zip)).getText().toString();
		
		if (isEmpty(email) || isEmpty(password) || isEmpty(username) ||
				isEmpty(firstName) || isEmpty(lastName) || isEmpty(zipCode)) {
			alert(R.string.all_fields_required);
		} else if (!validEmail(email)) {
			alert(R.string.invalid_email);
		} else if (!strongPassword(password)) {
			alert(R.string.new_passwords_not_strong);
		} else if (!password2.equals(password)) {
			alert(R.string.new_passwords_dont_match);
		} else if (!validZipCode(zipCode)) {
			alert(R.string.invalid_zip_code);
		} else {
			RequestGenerator rc = new RequestGenerator();
			User model = null;
			try {
				model = new User(username, firstName, lastName, 
						email, password, zipCode);
			} catch (JSONException e) {
				Log.e("Register", "error in User JSON.");
				e.printStackTrace();
				alert(R.string.problem_creating_account);
			}		
			try {
				rc.register(App.getInstance(), model, registrationResponseHandler);
			} catch (Exception e) {
				Log.e("Register", "exception in rc.addUser");
				e.printStackTrace();
				alert(R.string.problem_creating_account);	
			}
		}
	}	

	
	/*
	 * Response handlers
	 */
	private JsonHttpResponseHandler registrationResponseHandler = new JsonHttpResponseHandler() {
		public void onSuccess(JSONObject response) {
			if (responseIsSuccess(response)) {
				// TODO, ??? what is the difference between passing in app context versus
				// activity context?
				loginManager.logIn(App.getInstance(), username, password, afterLoginSendProfilePictureAndFinish);
			}else {
				Log.e("Register", response.toString());
				alert(R.string.problem_creating_account);
			}
		}
		public void onFailure(Throwable e, JSONObject response) {
			if (responseIsConflict(e, response)) {
				alert(R.string.username_is_taken);
			} else {
				Log.e("Register", response.toString() + "\n" + e.getMessage());
				alert(R.string.problem_creating_account);
			}
		}
	};
	private JsonHttpResponseHandler profilePictureResponseHandler = new JsonHttpResponseHandler() {
		public void onSuccess(JSONObject response) {				
			if (! responseIsSuccess(response)) {
				alert(R.string.problem_setting_profile_picture);
				Log.e("Register", "problem setting profile picture");
				Log.e("Register", response.toString());
			}
			notifyUserThatAcctCreatedAndReturnToProfile();
		}
		
		public void onFailure(Throwable e, JSONObject response) {
			alert(R.string.problem_setting_profile_picture);
			Log.e("Register", "problem setting profile picture");
			Log.e("Register", response.toString());
			Log.e("Register", e.getMessage());
		}
	};
	private Callback afterLoginSendProfilePictureAndFinish = new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			Bundle data = msg.getData();
			if(data.getBoolean("success")) {
				if (null == profilePicture) {
					notifyUserThatAcctCreatedAndReturnToProfile();
				} else {
					sendProfilePicture();
				}
				return true;
			} else {
				alert(R.string.problem_creating_account);
				return false;
			}
		}
		
	};

	
	/* 
	 * Picture Activity overrides
	 */
	@Override
	protected void submitBitmap(Bitmap bm) {
		profilePicture = bm;
		Log.d("Register", String.format("Bitmap dimensions: %d x %d",bm.getWidth(), bm.getHeight()));
		ImageView iv = (ImageView)findViewById(R.id.register_profilePic);
		iv.setImageBitmap(profilePicture);
		
		// this will be submitted later, when the user sends the registration request.
	}
	
	/* 
	 * Helper functions to display info to the  user
	 */
	private void notifyUserThatAcctCreatedAndReturnToProfile() {
		new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.done_registering)
        .setMessage(R.string.done_registering_msg)
        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	startActivity(new Intent(App.getInstance(), TabLayout.class));
            }
        })
        .show();
	};
	private void alert(int msg) {
		String s = this.getString(msg);
		Toast.makeText(App.getInstance(), s, Toast.LENGTH_LONG).show();		
	}

	
	/*
	 * Form validation functions
	 */
	private static boolean isEmpty(String field) {
		return field.length() == 0;
	}
	private static boolean validEmail(String email) {
		//TODO do we need an email regex?
		return true;
	}
	private static boolean strongPassword(String password) {
		return password.length() >= 6;
	}
	private static boolean validZipCode(String zipcode) {
		return true;
	}
	private static boolean responseIsSuccess(JSONObject response) {
		String status = "";
		try {
			status = response.getString("status");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return status.equals("success");
	}	
	
	/*
	 * Other helper functions
	 */
	private static boolean responseIsConflict(Throwable t, JSONObject response) {
		String msg = t.getMessage();
		Log.d("Register", msg);
		return msg.equals("CONFLICT");
	}	
	private void sendProfilePicture() {	
		RequestGenerator rc = new RequestGenerator();
		try {
			rc.addProfilePhoto(App.getInstance(), profilePicture, profilePictureResponseHandler);
		} catch (JSONException e) {
			alert(R.string.problem_setting_profile_picture);
			Log.e("Register", "Error formulating rc.addProfilePhoto request.");
			e.printStackTrace();
		}
	}
}