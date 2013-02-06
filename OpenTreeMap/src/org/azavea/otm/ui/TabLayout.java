package org.azavea.otm.ui;

import org.azavea.otm.R;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class TabLayout extends TabActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_layout);

        Resources res = getResources(); 
        TabHost tabHost = getTabHost(); 
        TabHost.TabSpec spec;  
        
        // Map
        Intent intent = new Intent().setClass(this, MainMapActivity.class);
        spec = tabHost.newTabSpec("map").setIndicator("Tree Map", 
        		res.getDrawable(R.drawable.tab_tree_map)).setContent(intent);
        tabHost.addTab(spec);

        // Profile
        intent = new Intent().setClass(this, ProfileDisplay.class);
        spec = tabHost.newTabSpec("profile").setIndicator("Profile", 
        		res.getDrawable(R.drawable.tab_profile)).setContent(intent);
        tabHost.addTab(spec);
        
        // List
        intent = new Intent().setClass(this, ListDisplay.class);
        spec = tabHost.newTabSpec("list").setIndicator("Lists", 
        		res.getDrawable(R.drawable.tab_list)).setContent(intent);
        tabHost.addTab(spec);
        
        // About
        intent = new Intent().setClass(this, AboutDisplay.class);
        spec = tabHost.newTabSpec("about").setIndicator("About", 
        		res.getDrawable(R.drawable.tab_about)).setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(3);
    }
}