package com.apple.login.kotlin

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.apple.login.R
import java.io.UnsupportedEncodingException
import java.util.*

class SampleActivity : AppCompatActivity() {

    private val TAG = "SampleActivity"
    private var mWebView: WebView? = null
    private var state: String? = null

    private val APPLE_CLIENT_ID = "com.your.client.id.here"
    private val APPLE_REDIRECT_URI = "https://your-redirect-uri.com/callback"
    private val APPLE_SCOPE = "name%20email"
    private val APPLE_AUTH_URL = "https://appleid.apple.com/auth/authorize"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        mWebView = findViewById(R.id.webView)

        val webSettings = mWebView?.settings
        webSettings?.javaScriptEnabled = true
        webSettings?.javaScriptCanOpenWindowsAutomatically = true
        mWebView?.webChromeClient = WebChromeClient()
        mWebView?.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                mWebView?.loadUrl("about:blank")
                val errorMessage = "Error $errorCode: $description [$failingUrl]"
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {}
            override fun onPageFinished(view: WebView?, url: String?) {}
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return isUrlOverridden(view, Uri.parse(url))
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return isUrlOverridden(view, request.url)
            }
        }
        state = getUUID()
        val url = (APPLE_AUTH_URL
                + "?response_type=code%20id_token&v=1.1.6&response_mode=form_post&client_id="
                + APPLE_CLIENT_ID + "&scope="
                + APPLE_SCOPE + "&state="
                + state + "&redirect_uri="
                + APPLE_REDIRECT_URI)

        mWebView?.loadUrl(url)
    }

    private fun getUUID(): String? {
        return UUID.randomUUID().toString()
    }

    private fun isUrlOverridden(view: WebView?, url: Uri?): Boolean {
        return when {
            url == null -> {
                false
            }
            url.toString().contains("appleid.apple.com") -> {
                view?.loadUrl(url.toString())
                true
            }
            url.toString().contains(APPLE_REDIRECT_URI) -> {
                val codeParam = url.getQueryParameter("code")
                val stateParam = url.getQueryParameter("state")
                val idTokenParam = url.getQueryParameter("id_token")
                val userParam = url.getQueryParameter("user")

                when {
                    codeParam == null -> {
                        Log.d(TAG, "code not returned")
                        Toast.makeText(applicationContext, "로그인 실패", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    stateParam != state -> {
                        Log.d(TAG, "state does not match")
                        Toast.makeText(applicationContext, "로그인 실패", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    else -> {
                        if(userParam != null)
                            Log.d(TAG, userParam)
                        if(idTokenParam != null)
                            jwtDecoded(idTokenParam)?.let { Log.d(TAG, it) }

                        Toast.makeText(applicationContext, "로그인 성공", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                true
            }
            else -> {
                false
            }
        }
    }

    private fun jwtDecoded(JWTEncoded: String): String? {
        var decodedJson = ""
        try {
            val split = JWTEncoded.split("\\.".toRegex()).toTypedArray()
            Log.d(TAG, "Header: " + getJson(split[0]))
            Log.d(TAG, "Body: " + getJson(split[1]))
            decodedJson = getJson(split[1])
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return decodedJson
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getJson(strEncoded: String): String {
        val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, charset("UTF-8"))
    }
}