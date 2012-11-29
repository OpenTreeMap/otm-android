package org.azavea.otm.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.azavea.otm.App;
import org.azavea.otm.R;

public class ChangePassword extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_password);
	}

	
	private boolean newPasswordsMatch() {
		String p1 = ((EditText)findViewById(R.id.newPassword1)).getText().toString();
		String p2 = ((EditText)findViewById(R.id.newPassword2)).getText().toString();
		return p1.equals(p2);
	}
	private boolean newPasswordMeetsStandard() {
		Editable p1 = ((EditText)findViewById(R.id.newPassword1)).getText();
		if (p1.length() < 6) {
			return false;
		} else {
			return true;
		}
	}
	private boolean oldPasswordIsBlank() {
		Editable old = ((EditText)findViewById(R.id.oldPassword)).getText();
		return (old.length()== 0);
		
	}
	private boolean newPasswordIsBlank() {
		Editable n = ((EditText)findViewById(R.id.newPassword1)).getText();
		return (n.length()== 0);
	}
	
	
	private boolean authorizedToChangePassword() {
		// TODO mtw
		return true;
	}
	
	private void alert(int msg) {
		String s = this.getString(msg);
		Toast.makeText(App.getInstance(), s, Toast.LENGTH_LONG).show();		
	}
	
	private boolean changePassword() {
		//TODO mtw.
		return false;
	}
	public void handleChangePasswordClick(View view) {
		if (oldPasswordIsBlank()) {
			alert(R.string.no_blank_old_password);
		} else if (newPasswordIsBlank()) {
			alert(R.string.no_blank_new_password);
		}
		else if ( ! authorizedToChangePassword()) {
			alert(R.string.bad_old_password);
		} else if (! newPasswordsMatch()) {
			alert(R.string.new_passwords_dont_match);
		} else if (! newPasswordMeetsStandard()) {
			alert(R.string.new_passwords_not_strong);
		} else {
			boolean success = changePassword();
			if (success) {
				alert(R.string.password_changed);
				
			} else {
				alert(R.string.password_change_error);
			}
		}
		return;
	}
}
