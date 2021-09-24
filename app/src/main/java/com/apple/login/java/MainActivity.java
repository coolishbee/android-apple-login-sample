package com.apple.login.java;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.apple.login.R;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private WebView mWebView;
    private String state;

    private final String APPLE_CLIENT_ID = "com.your.client.id.here";
    private final String APPLE_REDIRECT_URI = "https://your-redirect-uri.com/callback";
    private final String APPLE_SCOPE = "name%20email";
    private final String APPLE_AUTH_URL = "https://appleid.apple.com/auth/authorize";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mWebView = findViewById(R.id.webView);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                mWebView.loadUrl("about:blank");
                String errorMessage = "Error " + errorCode + ": " + description + " [" + failingUrl + "]";
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            @Override
            public void onPageFinished(WebView view, String url) {
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                return isUrlOverridden(view, Uri.parse(url));
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return isUrlOverridden(view, request.getUrl());
            }
        });

        state = getUUID();

        String url = APPLE_AUTH_URL
                + "?response_type=code%20id_token&v=1.1.6&response_mode=form_post&client_id="
                + APPLE_CLIENT_ID + "&scope="
                + APPLE_SCOPE + "&state="
                + state + "&redirect_uri="
                + APPLE_REDIRECT_URI;

        mWebView.loadUrl(url);
    }

    private String getUUID()
    {
        return UUID.randomUUID().toString();
    }

    private boolean isUrlOverridden(WebView view, Uri url)
    {
        boolean ret = false;
        if(url == null) {
            ret = false;
        }else if(url.toString().contains("appleid.apple.com")){
            Log.d(TAG, "appleid.apple.com");
            view.loadUrl(url.toString());
            ret = true;
        }else if (url.toString().contains(APPLE_REDIRECT_URI)){
            String codeParam = url.getQueryParameter("code");
            String stateParam = url.getQueryParameter("state");
            String idTokenParam = url.getQueryParameter("id_token");
            String userParam = url.getQueryParameter("user");

            if(codeParam == null){
                Log.d(TAG, "code not returned");
                Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();

            }else if(!stateParam.equals(state)){
                Log.d(TAG, "state does not match");
                Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();

            }else{

                if(userParam != null)
                    Log.d(TAG, userParam);
                if(idTokenParam != null)
                    Log.d(TAG, jwtDecoded(idTokenParam));

                Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();
            }
            ret = true;
        }

        return ret;
    }

    private String jwtDecoded(String JWTEncoded)
    {
        String decodedJson = "";
        try {
            String[] split = JWTEncoded.split("\\.");
            Log.d(TAG, "Header: " + getJson(split[0]));
            Log.d(TAG, "Body: " + getJson(split[1]));

            decodedJson = getJson(split[1]);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedJson;
    }

    private String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}