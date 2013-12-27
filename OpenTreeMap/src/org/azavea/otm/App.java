package org.azavea.otm;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.azavea.lists.NearbyList;
import org.azavea.otm.FilterManager;
import com.loopj.android.http.AsyncHttpClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

import org.azavea.otm.R;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * A global singleton object to maintain application state
 */
public class App extends Application {
	public static final String LOG_TAG = "AZ_OTM";
	
	private static App instance = null;
	private static LoginManager loginManager = null;
	private static FilterManager filterManager = null;
	private static FieldManager fieldManager = null;
	private static NearbyList nearbyList = null;
	
	private static SharedPreferences sharedPreferences = null;
	private static boolean pendingEnabled = false;
	private static InstanceInfo currentInstance = new InstanceInfo(16, "a87ff679a2f3e71d9181a67b7542122c","Test Instance");
	
	private static AsyncHttpClient asyncHttpClient;
	
	public static App getInstance() {
		checkInstance();
		return instance;
	}
	
	public static LoginManager getLoginManager() {
		if (loginManager == null) {
			checkInstance();
			loginManager = new LoginManager(instance);
			loginManager.autoLogin();
		}
		return loginManager;
	}

	/**
	 * Static access to single search manager instance
	 */
	public static FilterManager getFilterManager() {
		if (filterManager == null) {
			checkInstance();
			try {
				filterManager = new FilterManager(instance);
			} catch (Exception e) {
				Toast.makeText(instance, "Unable to access filter manager", 
						Toast.LENGTH_LONG).show();
				Log.e(LOG_TAG, "Unable to create filter manager", e);
			}
		}
		return filterManager;
	}
	
	/**
	 * Static access to single field manager instance
	 */
	public static FieldManager getFieldManager() {
		if (fieldManager == null) {
			checkInstance();
			try {
				fieldManager = new FieldManager(instance);
			} catch (Exception e) {
				Toast.makeText(instance, "Unable to access field manager", 
						Toast.LENGTH_LONG).show();
				Log.e(LOG_TAG, "Unable to create field manager", e);
			}
		}
		return fieldManager;
	}
	
	public static SharedPreferences getSharedPreferences() {
		if (sharedPreferences == null) {
			checkInstance();
			sharedPreferences = instance.getSharedPreferences(instance.getString(R.string.app_name), Context.MODE_PRIVATE);
			// Set-up SharedPreferences if they haven't been set up before
			setDefaultSharedPreferences(sharedPreferences);
		}
		return sharedPreferences;
	}

	public static boolean isPendingEnabled() {
		return pendingEnabled;
	}

	private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet");
    }
	
	// (re)Load the relevant resources into the
	// applications shared-preferences.
	private static void setDefaultSharedPreferences(SharedPreferences prefs) {
		Editor editor = prefs.edit();
		Context context = instance.getApplicationContext();
		editor.putString("base_url", context.getString(R.string.base_url))
			  .putString("tiler_url", context.getString(R.string.tiler_url))
			  .putString("plot_feature", context.getString(R.string.plot_feature))
			  .putString("boundary_feature", context.getString(R.string.boundary_feature))
			  .putString("canopy_tms_url", context.getString(R.string.canopy_tms_url))
			  .putString("image_url", context.getString(R.string.image_url))
			  .putString("api_key", context.getString(R.string.api_key))
			  .putString("max_nearby_plots", context.getString(R.string.max_nearby_plots))
			  .putString("start_lat", context.getString(R.string.start_lat))
			  .putString("start_lon", context.getString(R.string.start_lon))
			  .putString("search_bbox_lower_left_lat", context.getString(R.string.search_bbox_lower_left_lat))
			  .putString("search_bbox_lower_left_lon", context.getString(R.string.search_bbox_lower_left_lon))
			  .putString("search_bbox_upper_right_lat", context.getString(R.string.search_bbox_upper_right_lat))
			  .putString("search_bbox_upper_right_lon", context.getString(R.string.search_bbox_upper_right_lon))
			  .putString("starting_zoom_level", context.getString(R.string.starting_zoom_level))
			  .commit();
	
	}
	
	private static void loadPendingStatus() {
		// Load the pending setting from the included XML resource
		InputStream filterFile = App.getInstance().getResources().openRawResource(R.raw.configuration);
		try {
			DocumentBuilder xml = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xml.parse(filterFile);
			NodeList pending = doc.getElementsByTagName("pending");
			if (pending.getLength() > 0) {
				Node el = pending.item(0);
				pendingEnabled = Boolean.parseBoolean(el.getTextContent());
			}
		} catch (Exception e) {
			pendingEnabled = false;
			Log.e(LOG_TAG, "Invalid pending configuration xml file", e);
		}
	}
	
	@Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // Create an instance of login manager immediately, so that
        // the app can try to auto log in on any saved credentials
        getLoginManager();
        loadPendingStatus();
    }
	
	public static AsyncHttpClient getAsyncHttpClient() {
		if (asyncHttpClient == null) {
			asyncHttpClient = new AsyncHttpClient();
		}
		
		return asyncHttpClient;
	}
	
	public static NearbyList getNearbyList(Context context) {
		if (nearbyList == null) {
			nearbyList = new NearbyList(context );
		}
		
		return nearbyList;
	}
	
	public static LatLng getStartPos() {
		SharedPreferences prefs = getSharedPreferences();
		String lat = prefs.getString("start_lat", "");//("start_lat", "39.952622");
		String lon = prefs.getString("start_lon", "");//("start_lon", "-75.165708");
		double latd = Double.parseDouble(lat);
		double lond = Double.parseDouble(lon);
		return new LatLng(latd, lond);
		
	}

	public static InstanceInfo getCurrentInstance() {
		return currentInstance;
	}

	public static void setCurrentInstance(InstanceInfo currentInstance) {
		App.currentInstance = currentInstance;
	}	
}
