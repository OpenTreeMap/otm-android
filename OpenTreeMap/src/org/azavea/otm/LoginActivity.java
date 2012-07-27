package org.azavea.otm;

import org.azavea.otm.data.User;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private LoginManager loginManager = App.getLoginManager();
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
    }
    
    private boolean validate(String user, String pass) {
    	if ("".equals(user) || "".equals(pass)) {
    		String msg = "Please enter both a Username and a Password";
    		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    		return false;
    	}
    	return true;
    }
    
    public void login(View view) {
    	String username = ((EditText)findViewById(R.id.login_username))
    			.getText().toString().trim();
    	String password = ((EditText)findViewById(R.id.login_password))
    			.getText().toString().trim();
    	
    	if (validate(username, password)) {
    		
    		if (loginManager.logIn(username, password)) {
    			Intent next = new Intent(this, Download.class);
            	startActivity(next);
    		}
    		
        	
    	} 
    }
    

}
