package org.woheller69.weather.http;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.woheller69.weather.weather_api.IProcessHttpRequest;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * This class implements the IHttpRequest interface. It provides HTTP requests by using Volley.
 * See: https://developer.android.com/training/volley/simple.html
 */
public class VolleyHttpRequest implements IHttpRequest {

    private Context context;

    /**
     * Constructor.
     *
     * @param context Volley needs a context "for creating the cache dir".
     * @see Volley#newRequestQueue(Context)
     */
    public VolleyHttpRequest(Context context) {
        this.context = context;
    }

    /**
     * @see IHttpRequest#make(String, HttpRequestType, IProcessHttpRequest)
     */
    @Override
    public void make(String URL, HttpRequestType method, final IProcessHttpRequest requestProcessor) {
        RequestQueue queue = Volley.newRequestQueue(context, new HurlStack(null, getSocketFactory()));

        // Set the request method
        int requestMethod;
        switch (method) {
            case POST:
                requestMethod = Request.Method.POST;
                break;
            case GET:
                requestMethod = Request.Method.GET;
                break;
            case PUT:
                requestMethod = Request.Method.PUT;
                break;
            case DELETE:
                requestMethod = Request.Method.DELETE;
                break;
            default:
                requestMethod = Request.Method.GET;
        }

        // Execute the request and handle the response
        StringRequest stringRequest = new StringRequest(requestMethod, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        requestProcessor.processSuccessScenario(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        requestProcessor.processFailScenario(error);
                    }
                }
        );

        queue.add(stringRequest);
    }

    private SSLSocketFactory getSocketFactory() {

        CertificateFactory cf = null;
        try {
            // Load CAs from an InputStream
            cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(context.getAssets().open("SectigoRSADomainValidationSecureServerCA.crt"));

            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                Log.e("CERT", "ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory sf = context.getSocketFactory();

            return sf;

        } catch (CertificateException e) {
            Log.e("CERT", "CertificateException");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            Log.e("CERT", "NoSuchAlgorithmException");
            e.printStackTrace();
        } catch (KeyStoreException e) {
            Log.e("CERT", "KeyStoreException");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            Log.e("CERT", "FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("CERT", "IOException");
            e.printStackTrace();
        } catch (KeyManagementException e) {
            Log.e("CERT", "KeyManagementException");
            e.printStackTrace();
        }

        return null;
    }

}
