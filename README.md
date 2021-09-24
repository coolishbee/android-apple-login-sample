# android-appleLogin-sample

## 안드로이드 환경에서 애플 로그인을 사용하기 위한 설정

[참고 사이트](https://whitepaek.tistory.com/60)

## 로그인 요청 URL 설정

```kotlin
private val APPLE_CLIENT_ID = "com.your.client.id.here"
private val APPLE_REDIRECT_URI = "https://your-redirect-uri.com/callback"
private val APPLE_SCOPE = "name%20email"
private val APPLE_AUTH_URL = "https://appleid.apple.com/auth/authorize"

val url = (APPLE_AUTH_URL           
           + "?response_type=code%20id_token&v=1.1.6&response_mode=form_post&client_id="
           + APPLE_CLIENT_ID + "&scope="
           + APPLE_SCOPE + "&state="
           + state + "&redirect_uri="
           + APPLE_REDIRECT_URI)
```



## 로그인 웹뷰 구성

Java :

```java
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
```



Kotlin:

```kotlin
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
                            Log.d(TAG, jwtDecoded(idTokenParam))

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
```

## Redirect URI

redirect uri 설정시 애플서버에서 post 로 로그인정보를 request 할 것이다. 이때 반드시 서버내에서 redirect 를 해줘야 한다.

그래야 클라이언트 웹뷰에서 shouldOverrideUrlLoading 로 redirect 된 정보를 받을 수 있기 때문이다.

## 2차검증

Server to Server 로 진행하는 토큰검증하는 내용은 포함하고 있지 않습니다.

[별도로 참고할 사이트 안내해드립니다.](https://whitepaek.tistory.com/61)



> 출처
>
> https://stackoverflow.com/questions/37695877/how-can-i-decode-jwt-token-in-android
>
> 

