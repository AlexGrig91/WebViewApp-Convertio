package ru.startandroid.develop.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class ActivityButtonHome extends AppCompatActivity implements View.OnClickListener {

    Button buttonHome;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_home);

        String url = getIntent().getStringExtra(Constants.URL_EXTRA);
        buttonHome = (Button) findViewById(R.id.button);
        buttonHome.setOnClickListener(this);


        webView = (WebView) findViewById(R.id.webView);
//        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
//        webView.loadUrl("https://yandex.ru/");
//        Uri url = getIntent().getData();


        WebViewClient webViewClient = new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            @TargetApi(Build.VERSION_CODES.N) @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        };

        webView.setWebViewClient(webViewClient);
        webView.loadUrl(url);
    }

//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
//            webView.goBack();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button:
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            break;
        }
    }
}