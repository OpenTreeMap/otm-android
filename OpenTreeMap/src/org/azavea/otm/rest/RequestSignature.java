package org.azavea.otm.rest;

import java.net.URI;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.azavea.otm.App;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.loopj.android.http.RequestParams;

public class RequestSignature {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final String secretKey;

    public RequestSignature(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * All api calls are required to be signed using HMAC based on the request
     * string: {Http Verb}\n{host}\n{path}\n{k=v...}{body} where the query
     * parameters are byte ordered
     *
     * This assumes that the URL has been constructed with the access key and
     * the timestamp already appended.
     */
    public String getSignature(String verb, String url, RequestParams params, String body) throws Exception {
        // Add the params to the existing query string arguments, or add as new
        String separator = url.contains("?") ? "&" : "?";
        url += separator + params.toString();

        return getSignature(verb, url, body);
    }

    /**
     * All api calls are required to be signed using HMAC based on the request
     * string: {Http Verb}\n{host}\n{path}\n{k=v...}{body} where the query
     * parameters are byte ordered
     *
     * This assumes that the URL has been constructed with the access key and
     * the timestamp already appended.
     *
     */
    public String getSignature(String verb, String url, String body) throws Exception {

        URI uri = new URI(url);
        String hostWithPort = uri.getAuthority();
        String path = uri.getPath();

        // Signature is generated against query arguments sorted by key
        String[] query = uri.getQuery() != null ? uri.getQuery().split("&") : new String[] {};
        Arrays.sort(query);

        // The value of each query param must be URLEncoded, which isn't
        // reliable from URI.getQuery or .getRawQuery. Some values are
        // encoded on the fly during actual request, so do it manually here
        for (int i = 0; i < query.length; i++) {
            String[] kv = query[i].split("=");
            String encodedVal = URLEncoder.encode(kv[1]);
            query[i] = kv[0] + "=" + encodedVal;

        }
        String sortedQuery = TextUtils.join("&", query);

        String payload = verb + "\n" + hostWithPort + "\n" + path + "\n" + sortedQuery
                + Base64.encodeToString(body.getBytes("UTF-8"), Base64.NO_WRAP);

        String signature = calculateHMAC(payload);
        return signature;
    }

    /**
     * Generate HMAC API signature header to include in all requests
     */
    public Header getSignatureHeader(String verb, String url, RequestParams params) throws Exception {
        return getSignatureHeader(verb, url, params, "");
    }

    public Header getSignatureHeader(String verb, String url, String body) throws Exception {
        String sig = getSignature(verb, url, body);
        return new BasicHeader("X-Signature", sig);
    }

    public Header getSignatureHeader(String verb, String url, RequestParams params, String body) throws Exception {

        String sig = getSignature(verb, url, params, body);
        return new BasicHeader("X-Signature", sig);
    }

    /**
     * Computes RFC 2104-compliant HMAC signature.
     *
     * @param data
     *            The data to be signed.
     * @param key
     *            The signing key.
     * @return The Base64-encoded RFC 2104-compliant HMAC signature.
     * @throws SignatureException
     */
    public String calculateHMAC(String data) throws SignatureException {
        String result;
        try {
            // Get an hmac key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(this.secretKey.getBytes(), HMAC_ALGORITHM);

            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = Base64.encodeToString(rawHmac, Base64.NO_WRAP);

        } catch (Exception ex) {
            Log.e(App.LOG_TAG, "Failed to generate HMAC for API:" + ex.getMessage());
            throw new SignatureException("Could not sign API request");
        }
        return result;
    }
}
