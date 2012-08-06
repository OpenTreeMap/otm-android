package org.azavea.otm.ui;

import org.azavea.otm.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ProfileDisplay extends Activity{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
    }
    
    public void showLogin(View button) {
    	Intent login = new Intent(this, LoginActivity.class);
    	startActivity(login);
	}
}
