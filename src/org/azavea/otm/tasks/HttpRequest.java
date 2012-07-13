package org.azavea.otm.tasks;

import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.azavea.otm.Download;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public class HttpRequest extends AsyncTask<String, Void, String> {

	public Download DownloadActivity;
	public HttpRequest(Download download) {
		Log.d("AZ", "Constructed");	
		DownloadActivity = download;
	}

	@Override
	protected String doInBackground(String... uris) {
		AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		try {
			Log.d("AZ", "URL: " + uris[0]);
			URI uri = new URI(uris[0]);
			HttpGet get = new HttpGet(uri);

			HttpResponse resp = client.execute(get);
			Log.d("AZ", "Status: " + resp.getStatusLine().getStatusCode());
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = resp.getEntity();
				if (entity != null) {
					String body = new String(EntityUtils.toString(entity).getBytes());
					Log.d("AZ", "Response: " + body);
					return body;
				}
			}
		}
		catch (Exception e){
			Log.e("AZ", e.toString());
		}
		finally {
			client.close();
		}
		return null;
	}

    protected void onPostExecute(String result) {
       DownloadActivity.showResult(result);
    }
}
