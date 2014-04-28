package org.azavea.otm.rest;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.azavea.otm.App;
import org.azavea.otm.data.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

// This class is designed to take care of the base-url
// and otm api-key for REST requests
public class RestClient {
    private final String apiUrl;

    private final String baseUrl;

    private String host;

    private AsyncHttpClient client;

    private final SharedPreferences prefs;

    private final String appVersion;

    private final RequestSignature reqSigner;

    public RestClient() {
        prefs = App.getSharedPreferences();
        apiUrl = getApiUrl();
        baseUrl = getBaseUrl();
        appVersion = getAppVersion();
        client = createHttpClient();
        reqSigner = new RequestSignature(prefs.getString("secret_key", ""));

        // The underlying request mechanism doesn't appear to set the HOST
        // header correctly, so include the header manually - it is required
        // to generate a matching signature on the api server.
        try {
            // Authority is servername[:port] if port is not 80
            host = new URI(apiUrl).getAuthority();
        } catch (URISyntaxException e) {
            Log.e(App.LOG_TAG, "Could not determine valid HOST from base URL");
        }
    }

    // Dependency injection to support mocking
    // in unit-tests
    public void setAsyncClient(AsyncHttpClient client) {
        this.client = client;
    }

    public void cancelRequests(Context context) {
        client.cancelRequests(context, true);
    }

    private void get(String url, RequestParams params,
            ArrayList<Header> headers, AsyncHttpResponseHandler responseHandler) {
        if (headers == null) {
            headers = new ArrayList<Header>();
        }

        try {
            String reqUrl = getAbsoluteUrl(url);
            RequestParams reqParams = prepareParams(params);
            headers.add(reqSigner.getSignatureHeader("GET", reqUrl, reqParams));

            Header[] fullHeaders = prepareHeaders(headers);

            client.get(App.getAppInstance(), reqUrl, fullHeaders, reqParams,
                    responseHandler);
        } catch (Exception e) {
            String msg = "Failure making GET request";
            Log.e(App.LOG_TAG, msg, e);
            responseHandler.onFailure(e, msg);
        }
    }

    /**
     * Signed GET request with no authentication
     */
    public void get(String url, RequestParams params,
            AsyncHttpResponseHandler responseHandler) {

        this.get(url, params, null, responseHandler);
    }

    /**
     * Signed GET request with basic authentication headers
     */
    public void getWithAuthentication(String url, String username,
            String password, RequestParams params,
            AsyncHttpResponseHandler responseHandler) {

        Header[] authHeader =
            { createBasicAuthenticationHeader(username, password) };
        this.get(url, params, new ArrayList<Header>(Arrays.asList(authHeader)),
                responseHandler);
    }

    public void post(Context context, String url, int id, Model model,
            AsyncHttpResponseHandler response)
            throws UnsupportedEncodingException {
        String completeUrl = getAbsoluteUrl(url) + id;
        completeUrl = prepareUrl(completeUrl);
        client.post(context, completeUrl, new StringEntity(model.getData()
                .toString()), "application/json", response);
    }

    public void post(Context context, String url, Model model,
            AsyncHttpResponseHandler response)
            throws UnsupportedEncodingException {
        String completeUrl = getAbsoluteUrl(url);
        completeUrl = prepareUrl(completeUrl);
        client.post(context, completeUrl, new StringEntity(model.getData()
                .toString()), "application/json", response);
    }

    public void post(String url, RequestParams params,
            AsyncHttpResponseHandler responseHandler) {
        RequestParams reqParams = prepareParams(params);
        client.post(getAbsoluteUrl(url), reqParams, responseHandler);
    }

    private void put (String url, int id,
            ArrayList<Header> headers,
            String body,
            AsyncHttpResponseHandler responseHandler){

        try {
            String reqUrl = safePathJoin(getAbsoluteUrl(url),
                    id == -1 ? "" : Integer.toString(id));
            String reqUrlWithParams = prepareUrl(reqUrl);
            if (headers == null) {
                headers = new ArrayList<Header>();
            }
            headers.add(reqSigner.getSignatureHeader("PUT", reqUrlWithParams, body));

            Header[] fullHeaders = prepareHeaders(headers);

            StringEntity bodyEntity = new StringEntity(body, "UTF-8");
            client.put(App.getAppInstance(), reqUrlWithParams, fullHeaders,
                    bodyEntity, "application/json", responseHandler);

        } catch (Exception e) {
            String msg = "Failure making PUT request";
            Log.e(App.LOG_TAG, msg, e);
            responseHandler.onFailure(e, msg);
        }
    }

    public void put(String url, int id, Model model,
            AsyncHttpResponseHandler response) {

        put(url, id, null, model.getData().toString(), response);
    }

    /**
     * Executes a put request and adds basic authentication headers to the
     * request.
     */
    public void putWithAuthentication(String url,
            String username, String password, int id, Model model,
            AsyncHttpResponseHandler response)
            throws UnsupportedEncodingException {

        Header[] headers = { createBasicAuthenticationHeader(username, password) };
        String body = model.getData().toString();

        put(url, id, new ArrayList<Header>(Arrays.asList(headers)),
                body, response);
    }

    public void putWithAuthentication(String url,
            String username, String password, Model model,
            AsyncHttpResponseHandler response)
            throws UnsupportedEncodingException {

        Header[] headers = { createBasicAuthenticationHeader(username, password) };
        String body = model.getData().toString();

        put(url, -1, new ArrayList<Header>(Arrays.asList(headers)),
                body, response);
    }


    private void post (String url, ArrayList<Header> headers, String body,
            AsyncHttpResponseHandler responseHandler){

        try {
            String reqUrl = getAbsoluteUrl(url);
            String reqUrlWithParams = prepareUrl(reqUrl);
            if (headers == null) {
                headers = new ArrayList<Header>();
            }
            headers.add(reqSigner.getSignatureHeader("POST", reqUrlWithParams, body));

            Header[] fullHeaders = prepareHeaders(headers);

            StringEntity bodyEntity = new StringEntity(body, "UTF-8");
            client.post(App.getAppInstance(), reqUrlWithParams, fullHeaders,
                    bodyEntity, "application/json", responseHandler);

        } catch (Exception e) {
            String msg = "Failure making POST request";
            Log.e(App.LOG_TAG, msg, e);
            responseHandler.onFailure(e, msg);
        }
    }

    /**
     * Executes a post request and adds basic authentication headers to the
     * request.
     */
    public void postWithAuthentication(String url, String username, String password,
            Model model, AsyncHttpResponseHandler responseHandler)
            throws UnsupportedEncodingException {

        Header[] headers = { createBasicAuthenticationHeader(username, password) };
        String body = null;
        if (model != null) {
            body = model.getData().toString();
        }

        post(url, new ArrayList<Header>(Arrays.asList(headers)),
                body, responseHandler);
    }

    public void postWithAuthentication(String url, String username, String password,
            AsyncHttpResponseHandler responseHandler)
            throws UnsupportedEncodingException {

        postWithAuthentication(url, username, password, null, responseHandler);
    }

    // This overloading of the postWithAuthentication method takes a bitmap, and
    // posts it as an PNG HTTP Entity.
    public void postWithAuthentication(String url, Bitmap bm,
            String username, String password,
            JsonHttpResponseHandler responseHandler, int timeout) {

        String completeUrl = getAbsoluteUrl(url);
        completeUrl = prepareUrl(completeUrl);

        // Content type also needs to be pinned down in the Bitmap.compress
        // call, which is why I haven't exposed it as a parameter.
        String contentType = "image/jpeg";

        // We need to coerce the bitmap into a ByteArrayEntity so that we can
        // post it.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(CompressFormat.JPEG, 55, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayEntity bae = new ByteArrayEntity(bitmapdata);

        // No signature for http client which takes a BitmapEntity and a headers
        // array, so creating a one-off client for this purpose
        AsyncHttpClient authenticatedClient = createAutheniticatedHttpClient(
                username, password);
       
        try {
            // Add the signature based on the base64 encoded representation of the bitmap
            Header sig = reqSigner.getSignatureHeader("POST", completeUrl, bitmapdata);
            authenticatedClient.addHeader(sig.getName(), sig.getValue());
        } catch (Exception e) {
            responseHandler.onFailure(e, "Unable to sign photo upload request");
            return;
        }

        authenticatedClient.setTimeout(timeout);
        authenticatedClient.post(App.getAppInstance(), completeUrl, bae, contentType,
                responseHandler);
    }

    public void delete(String url, AsyncHttpResponseHandler responseHandler) {
        client.delete(getAbsoluteUrl(url), responseHandler);
    }

    /**
     * Executes a delete request and adds basic authentication headers to the
     * request.
     */
    public void deleteWithAuthentication(Context context, String url,
            String username, String password,
            AsyncHttpResponseHandler responseHandler) {
        String completeUrl = getAbsoluteUrl(url);
        completeUrl = prepareUrl(completeUrl);
        Header[] headers = { createBasicAuthenticationHeader(username, password) };
        client.delete(context, completeUrl, headers, responseHandler);
    }

    public void getImage(String imageUrl, BinaryHttpResponseHandler handler) {
        if (imageUrl.startsWith("/")) {
            imageUrl = safePathJoin(baseUrl, imageUrl);
        }
        client.get(imageUrl, handler);
    }

    private RequestParams prepareParams(RequestParams params) {
        // We'll always need a RequestParams object since we'll always
        // be sending credentials
        RequestParams reqParams;
        if (params == null) {
            reqParams = new RequestParams();
        } else {
            reqParams = params;
        }

        reqParams.put("timestamp", getTimestamp());
        reqParams.put("access_key", getAccessKey());

        return reqParams;
    }

    private String prepareUrl(String url) {
        // Not all methods of AsynchHttpClient take a requestParams.
        // Sometimes we will need to put the api key and other data
        // directly in the URL.
        return url + "?" + getTimestampQuery() + "&" + getAccessKeyQuery();
    }

    /**
     * Ensure all required headers are present
     *
     * @param additionalHeaders
     *            List of headers specific to a single request
     * @return Complete list of headers necessary for API request
     */
    private Header[] prepareHeaders(ArrayList<Header> additionalHeaders) {
        BasicHeader defaultHeader = new BasicHeader("Host", host);

        if (additionalHeaders != null) {
            ArrayList<Header> headers =
                    (ArrayList<Header>) additionalHeaders.clone();
            headers.add(defaultHeader);

            return headers.toArray(new Header[headers.size()]);
        } else {
            return new Header[] { defaultHeader };
        }
    }

    /***
     * Current timestamp string in UTC format for API request verification
     *
     * @return Query string format of "timestamp={UTC Timestamp}"
     */
    private String getTimestampQuery() {
        return "timestamp=" + getTimestamp();
    }

    /***
     *
     * @return Current timestamp string in UTC format for API request
     *         verification
     */
    private String getTimestamp() {
        SimpleDateFormat dateFormatUtc = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss");
        dateFormatUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormatUtc.format(new Date());
    }

    /***
     * Configured Access Key for API request verification
     *
     * @return Query string format of "access_key={ACCESSKEY}"
     */
    private String getAccessKeyQuery() {
        return "access_key=" + getAccessKey();
    }

    private String getAccessKey() {
        return prefs.getString("access_key", "");
    }

    private String getApiUrl() {
        String apiUrl = prefs.getString("api_url", "");
        return apiUrl;
    }

    private String getBaseUrl() {
        String baseUrl = prefs.getString("base_url", "");
        return baseUrl;
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return safePathJoin(apiUrl, relativeUrl);
    }

    private String safePathJoin(String base, String path) {
        String cleanBase = base;
        String cleanPath = path;
        if (base.charAt(base.length() - 1) == '/') {
            cleanBase = base.substring(0, base.length() - 1);
        }

        if (!TextUtils.isEmpty(path) && path.charAt(0) == '/') {
            cleanPath = path.substring(1);
        } else if (TextUtils.isEmpty(path)) {
            return cleanBase;
        }
        return cleanBase + "/" + cleanPath;
    }

    private Header createBasicAuthenticationHeader(String username,
            String password) {
        String credentials = String.format("%s:%s", username, password);
        String encoded = Base64.encodeToString(credentials.getBytes(),
                Base64.NO_WRAP);
        return new BasicHeader("Authorization", String.format("%s %s", "Basic",
                encoded));
    }

    private AsyncHttpClient createHttpClient() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("platform-ver-build", appVersion);
        return client;
    }

    private AsyncHttpClient createAutheniticatedHttpClient(String username,
            String password) {
        AsyncHttpClient client = createHttpClient();
        Header header = createBasicAuthenticationHeader(username, password);
        client.addHeader(header.getName(), header.getValue());
        return client;
    }

    private String getAppVersion() {
        return prefs.getString("platform_ver_build", "");
    }
}
