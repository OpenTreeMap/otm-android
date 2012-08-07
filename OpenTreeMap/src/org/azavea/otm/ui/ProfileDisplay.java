package org.azavea.otm.ui;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.User;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ProfileDisplay extends Activity{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        loadProfile();
    }
    
    private void loadProfile() {
    	TextView text = (TextView)findViewById(R.id.textView1);
        if (App.getLoginManager().isLoggedIn()) {
        	User user = App.getLoginManager().loggedInUser;
        	try {
				text.setText("You are: " + user.getUserName());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	findViewById(R.id.login).setVisibility(View.INVISIBLE);
        	findViewById(R.id.logout).setVisibility(View.VISIBLE);
        } else {
        	text.setText("Profile");
        	findViewById(R.id.login).setVisibility(View.VISIBLE);
        	findViewById(R.id.logout).setVisibility(View.INVISIBLE);
        }    	
    }
    
    public void showLogin(View button) {
    	Intent login = new Intent(this, LoginActivity.class);
    	startActivityForResult(login, 0);
	}
    
    public void logout(View button) {
    	App.getLoginManager().logOut();
    	loadProfile();
    }
    
    protected void onActivityResult(int requestCode, int resultCode, 
    		Intent data) {
    	if (resultCode == RESULT_OK) {
    		Log.d(App.LOG_TAG, "Reload profile for new user login");
    		loadProfile();
    	} else if (resultCode == RESULT_CANCELED) {
    		// Nothing?
    	}
    }
}
