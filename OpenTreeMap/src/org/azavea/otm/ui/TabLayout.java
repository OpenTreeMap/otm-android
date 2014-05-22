package org.azavea.otm.ui;

import org.azavea.otm.R;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;

public class TabLayout extends ActionBarActivity {

    private static final String SELECTED_TAB = "TAB";

    private static final String MAIN_MAP = "MainMapActivity";
    private static final String PROFILE = "ProfileDisplay";
    private static final String LISTS = "ListDisplay";
    private static final String ABOUT = "AboutDisplay";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Map
        actionBar.addTab(
                actionBar.newTab()
                        .setText(R.string.tab_map)
                        .setTag(MAIN_MAP)
                        .setTabListener(new TabListener<MainMapActivity>(this, MAIN_MAP, MainMapActivity.class))
        );

        actionBar.addTab(
                actionBar.newTab()
                        .setText(R.string.tab_profile)
                        .setTabListener(new TabListener<ProfileDisplay>(this, PROFILE, ProfileDisplay.class))
        );

        actionBar.addTab(
                actionBar.newTab()
                        .setText(R.string.tab_lists)
                        .setTabListener(new TabListener<ListDisplay>(this, LISTS, ListDisplay.class))
        );

        actionBar.addTab(
                actionBar.newTab()
                        .setText(R.string.tab_about)
                        .setTabListener(new TabListener<AboutDisplay>(this, ABOUT, AboutDisplay.class))
        );

        if (savedInstanceState != null) {
            actionBar.setSelectedNavigationItem(savedInstanceState.getInt(SELECTED_TAB));
        }
    }

    @Override
    public void onBackPressed() {
        // A bit of an annoyance, the TabLayout Activity gets the backpress events
        // and must delegate them back down to the MainMapActivity Fragment
        // If we need to support handling back presses differently on each tab,
        // we should probably make an Interface and call whatever the current tab is
        ActionBar actionBar = getSupportActionBar();
        if (actionBar.getSelectedTab().getTag() == MAIN_MAP) {
            final FragmentManager manager = TabLayout.this.getSupportFragmentManager();
            MainMapActivity mainMap = (MainMapActivity)manager.findFragmentByTag(MAIN_MAP);
            mainMap.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(SELECTED_TAB, getSupportActionBar().getSelectedNavigationIndex());
    }

    public class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment tabFragment;
        private final ActionBarActivity host;
        private final String tag;
        private final Class<T> tabClass;

        /** Constructor used each time a new tab is created.
         * @param host  The host Activity, used to instantiate the fragment
         * @param tag  The identifier tag for the fragment
         * @param clz  The fragment's Class, used to instantiate the fragment
         */
        public TabListener(ActionBarActivity host, String tag, Class<T> clz) {
            this.host = host;
            this.tag = tag;
            tabClass = clz;

            final FragmentManager manager = TabLayout.this.getSupportFragmentManager();
            tabFragment = manager.findFragmentByTag(tag);
            if (tabFragment != null && !tabFragment.isHidden()) {
                manager.beginTransaction().hide(tabFragment).commit();
            }
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            // Check if the fragment is already initialized
            if (tabFragment == null) {
                // If not, instantiate and add it to the activity
                tabFragment = Fragment.instantiate(host, tabClass.getName());
                ft.add(android.R.id.content, tabFragment, tag);
            } else {
                // If it exists, simply attach it in order to show it
                ft.show(tabFragment);
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (tabFragment != null) {
                // Detach the fragment, because another one is being attached
                ft.hide(tabFragment);
            }
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    }
}