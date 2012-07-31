package org.azavea.otm;


import org.azavea.otm.rest.handlers.CBack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
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
    		loginManager.logIn(username, password, new Callback() {

				@Override
				public boolean handleMessage(Message msg) {
					Bundle data = msg.getData();
					if(data != null && data.getBoolean("success")) {
						Intent next = new Intent(App.getInstance(), Download.class);
			            startActivity(next);
			            return true;
					} else {
						Toast.makeText(App.getInstance(), 
								"Username or Password incorrect,  please try again", 
								Toast.LENGTH_LONG).show();
						return false;
					}
					
				} 
			});
    		
    	}
    }
    

}
