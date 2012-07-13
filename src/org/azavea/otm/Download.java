package org.azavea.otm;

import org.azavea.otm.tasks.HttpRequest;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Download extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void fetch(View view) {
    	
    	EditText url = (EditText)findViewById(R.id.edit_message);
    	String text = url.getText().toString();
    	new HttpRequest(this).execute(text);
    }
    
    public void showMap(View view) {
    	// Create intent for map-view activity and switch
    	Intent intent = new Intent(view.getContext(), MapDisplay.class);
    	startActivity(intent);
    	
    }
    
    public void showResult(String result) {
        TextView textView = new TextView(this);
        textView.setTextSize(10);
        textView.setText(result);

        setContentView(textView);    	
    }
}
