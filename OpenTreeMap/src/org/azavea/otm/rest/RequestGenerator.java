package org.azavea.otm.rest;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.azavea.otm.App;
import org.azavea.otm.InstanceInfo;
import org.azavea.otm.LoginManager;
import org.azavea.otm.data.Model;
import org.azavea.otm.data.Password;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.PlotContainer;
import org.azavea.otm.data.User;
import org.azavea.otm.rest.handlers.ContainerRestHandler;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class RequestGenerator {
	private RestClient client;
	LoginManager loginManager = App.getLoginManager();

	private static int PHOTOUPLOADTIMEOUT = 30000;

	public RequestGenerator() {
		client = new RestClient();
	}

	// Dependency injection to support unit-testing
	public void setClient(RestClient client) {
		this.client = client;
	}

	/**
	 * Cancel any pending or active requests started from the provided context
	 */
	public void cancelRequests(Context activityContext) {
		client.cancelRequests(activityContext);
	}

	public void getVersion(JsonHttpResponseHandler handler) {
		client.get("/version", null, handler);
	}

	public void getPlot(int id, JsonHttpResponseHandler handler) {
		client.get("/plots/" + id, null, handler);
	}

    public void getImage(String imageUrl, BinaryHttpResponseHandler binaryHttpResponseHandler) {
        client.getImage(imageUrl, binaryHttpResponseHandler);
    }

	public void getPlotsNearLocation(double geoY, double geoX, RequestParams rp,  
	        ContainerRestHandler<PlotContainer> handler) {
		String url = getInstanceNameUri(String.format("locations/%s,%s/plots", geoY, geoX));

		try {
			if (loginManager.isLoggedIn()) {
				client.getWithAuthentication(url,
						loginManager.loggedInUser.getUserName(),
						loginManager.loggedInUser.getPassword(),
						rp, handler);
			} else {
				client.get(url, rp, handler);
			}
		} catch (JSONException e) {
			// If user json error, request with no auth
			client.get(url, null, handler);
		}
	}

	/**
	 * Request information on a specific OTM instance
	 * @param urlName Short URL slug name of instance
	 */
	public void getInstanceInfo(String urlName,
	        JsonHttpResponseHandler handler) {

	    // Note: Public instances do not require auth, but private do.
	    // This will need to be modified when we support private maps
	    String url = "/instance/" + urlName;
	    User user = loginManager.loggedInUser;

	    try {
            if (loginManager.isLoggedIn()) {
                client.getWithAuthentication(url,
                        user.getUserName(),
                        user.getPassword(),
                        null, handler);
            } else {
                client.get(url, null, handler);
            }
	    } catch (JSONException e) {
                client.get(url, null, handler);
	    }
	}

	private String getInstanceNameUri(String path) {
		InstanceInfo instance = App.getAppInstance().getCurrentInstance();
		if (path.charAt(0) == '/') {
		    path = path.substring(1);
		}
		if (instance != null) {
			return "/instance/" + instance.getUrlName() + "/" + path;
		}
		return "";
	}

	public void getPlotsNearLocation(double geoY, double geoX, boolean recent, boolean pending,
			ContainerRestHandler<PlotContainer> handler) {
		SharedPreferences sharedPrefs = App.getSharedPreferences();
		String maxPlots = sharedPrefs.getString("max_nearby_plots", "10");

		RequestParams params = new RequestParams();
		params.put("max_plots", maxPlots);
		params.put("filter_recent", Boolean.toString(recent));
		params.put("filter_pending", Boolean.toString(pending));
		
		getPlotsNearLocation(geoY, geoX, params, handler);
	}

	public void updatePlot(int id, Plot plot,
			AsyncHttpResponseHandler handler) throws UnsupportedEncodingException {
		if (loginManager.isLoggedIn()) {
			try {
				client.putWithAuthentication(getInstanceNameUri("plots/"),
						loginManager.loggedInUser.getUserName(),
						loginManager.loggedInUser.getPassword(), id, plot, handler);
			} catch (JSONException e) {
				handleBadResponse(e);
			}
		} else {
			redirectToLoginActivity();
		}
	}

	public void getUserEdits(Context context, User user, int offset, int count, AsyncHttpResponseHandler handler)
			throws JSONException {
		if (user != null) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("offset", Integer.toString(offset));
			params.put("length", Integer.toString(count));

			client.getWithAuthentication("/user/" + user.getId() + "/edits",
					loginManager.loggedInUser.getUserName(),
					loginManager.loggedInUser.getPassword(),
						new RequestParams(params), handler);
		}
	}

	public void addUser(User user, AsyncHttpResponseHandler handler)
			throws JSONException, UnsupportedEncodingException {
		client.postWithAuthentication("/user/",
				loginManager.loggedInUser.getUserName(),
				loginManager.loggedInUser.getPassword(), user, handler);
	}

	public void logIn(Context context, String username, String password,
			JsonHttpResponseHandler handler) {
			client.getWithAuthentication("/user", username, password, null, handler);
	}

	public void getAllSpecies(JsonHttpResponseHandler handler) {
		client.get(getInstanceNameUri("species"), null, handler);
	}

	public void deleteCurrentTreeOnPlot(Context context, int plotId, JsonHttpResponseHandler handler)
			throws JSONException {

		client.deleteWithAuthentication(context, "/plots/" + plotId + "/tree",
				loginManager.loggedInUser.getUserName(), loginManager.loggedInUser.getPassword(),
				handler);
	}

	public void deletePlot(Context context, int plotId, JsonHttpResponseHandler handler)
			throws JSONException {

		client.deleteWithAuthentication(context, "/plots/" + plotId,
				loginManager.loggedInUser.getUserName(), loginManager.loggedInUser.getPassword(),
				handler);
	}

	public void addTreePhoto(Plot plot, Bitmap bm,
			JsonHttpResponseHandler handler)
			throws JSONException {
	    String formattedPath = String.format("plots/%s/tree/photo", plot.getId());
		client.postWithAuthentication(getInstanceNameUri(formattedPath), bm,
				loginManager.loggedInUser.getUserName(), 
				loginManager.loggedInUser.getPassword(),
				handler, PHOTOUPLOADTIMEOUT);
	}

	public void addProfilePhoto(Bitmap bm, JsonHttpResponseHandler handler)
			throws JSONException {
	    User user = loginManager.loggedInUser;
	    String formattedPath = String.format("user/%s/photo/profile", user.getId());
		client.postWithAuthentication(formattedPath, bm,
				user.getUserName(), user.getPassword(),
				handler, PHOTOUPLOADTIMEOUT);

	}

	public void changePassword(Context context, String newPassword, JsonHttpResponseHandler handler)
	        throws JSONException, UnsupportedEncodingException {
		Model password = new Password(newPassword);

		client.putWithAuthentication("/user/" + loginManager.loggedInUser.getId(),
				loginManager.loggedInUser.getUserName(),
				loginManager.loggedInUser.getPassword(),
				password, handler);
	}

	private void handleBadResponse(JSONException e) {
		Log.e(App.LOG_TAG, "Unable to parse JSON response", e);
	}

	private void redirectToLoginActivity() {
		// TODO Auto-generated method stub

	}

	public void register(Context context, User user, JsonHttpResponseHandler handler) throws UnsupportedEncodingException {
		client.post(context, "/user", user, handler);
	}

	public void addTree(Plot plot, AsyncHttpResponseHandler handler)
			throws JSONException, UnsupportedEncodingException {

		client.postWithAuthentication(getInstanceNameUri("plots"),
				loginManager.loggedInUser.getUserName(),
				loginManager.loggedInUser.getPassword(), plot, handler);
	}

	public void rejectPendingEdit(int id, JsonHttpResponseHandler handler) throws UnsupportedEncodingException, JSONException {
		String url = String.format("/pending-edits/%d/reject", id);
		client.postWithAuthentication(url, loginManager.loggedInUser.getUserName(),
				loginManager.loggedInUser.getPassword(), handler);
	}

	public void approvePendingEdit(int id, JsonHttpResponseHandler handler) throws UnsupportedEncodingException, JSONException {
		String url = String.format("/pending-edits/%d/approve", id);
		client.postWithAuthentication(url, loginManager.loggedInUser.getUserName(),
				loginManager.loggedInUser.getPassword(), handler);
	}
}
