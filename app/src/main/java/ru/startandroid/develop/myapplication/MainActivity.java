package ru.startandroid.develop.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULT_CODE = 1;
    public ValueCallback<Uri[]> uploadMessage;
    private WebView webView;
    private ValueCallback<Uri> mUploadMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {

            //позволяем приложению обрабатывать ссылки
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //if (Uri.parse(url).getHost().equals("https://convertio.co/")){
                //https://convertio.co/ru/download/d3e3160ed87ea17f900e1167324e2d6c7802cb/
                //https://convertio.co/ru/download/d3e3160ed87ea17f900e1167324e2d6c7802cb/
                //https://convertio.co/ru/download/d3e3160ed87ea17f900e1167324e2d6c706807/
                if (Uri.parse(url).getHost().startsWith("convertio.co")) {
                    view.loadUrl(url);
                } else {
                    openSideUrl(url);
                }
                return true;
            }

        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.loadUrl("https://convertio.co/ru/jpg-png/");

        webView.setWebChromeClient(new WebChromeClient() {

            //открываем File Chooser
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    Toast.makeText(MainActivity.this.getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }

            //выбираем изображение
            protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULT_CODE);
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            //загружаем изображение
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                request.setMimeType(mimeType);
                //------------------------COOKIE!!------------------------
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                //------------------------COOKIE!!------------------------
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openSideUrl(String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Вы переходите по сторонней ссылке")
                .setMessage("Желаете открыть в стороннем браузере")
                .setPositiveButton(R.string.in_browser, (dialog, which) -> {
                    openLinkBrowser(url);
                })
                .setNegativeButton(R.string.in_app, (dialog, which) -> {
                    openLinkApp(url);
                });

        AlertDialog dialog = builder.create();
//        dialog.setOnDismissListener((l) -> {
//            Toast.makeText(this, "Dismissed", Toast.LENGTH_SHORT).show();
//        });
        dialog.show();
    }

    private void openLinkBrowser(String url) {
        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "text/html");
        startActivity(Intent.createChooser(intent, "Complete action using"));
//                startActivity(
//                        Intent.makeMainSelectorActivity(
//                                Intent.ACTION_MAIN,
//                                Intent.CATEGORY_APP_BROWSER));
    }

    private void openLinkApp(String url) {
        Intent intent = new Intent(this, ActivityButtonHome.class);
        intent.putExtra(Constants.URL_EXTRA, url);
        startActivity(intent);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULT_CODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else
            Toast.makeText(MainActivity.this.getApplicationContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }

    //если перешли на другую страницу и нажали back возвращаемся назад а не выходим из программы
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MyBrowser extends WebViewClient {

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            view.loadData("Sorry Your Internet is not stable", "text/html", "utf-8");
            super.onReceivedError(view, request, error);
        }

    }
}




