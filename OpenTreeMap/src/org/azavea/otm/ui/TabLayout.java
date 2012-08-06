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
        Intent intent = new Intent().setClass(this, MapDisplay.class);
        spec = tabHost.newTabSpec("map").setIndicator("Tree Map", 
        		res.getDrawable(android.R.drawable.ic_menu_mapmode)).setContent(intent);
        tabHost.addTab(spec);

        // Profile
        intent = new Intent().setClass(this, ProfileDisplay.class);
        spec = tabHost.newTabSpec("profile").setIndicator("Profile", 
        		res.getDrawable(android.R.drawable.ic_menu_mapmode)).setContent(intent);
        tabHost.addTab(spec);
        
        // List
        intent = new Intent().setClass(this, ListDisplay.class);
        spec = tabHost.newTabSpec("list").setIndicator("Lists", 
        		res.getDrawable(android.R.drawable.ic_menu_sort_by_size)).setContent(intent);
        tabHost.addTab(spec);
        
        // About
        intent = new Intent().setClass(this, AboutDisplay.class);
        spec = tabHost.newTabSpec("about").setIndicator("About", 
        		res.getDrawable(android.R.drawable.ic_menu_info_details)).setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(3);
    }
}