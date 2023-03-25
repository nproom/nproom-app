package com.csoftsolutions.nproom;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final int FILE_PICKER_REQUEST_CODE = 1;
    private WebView webView;

    @SuppressLint({"MissingInflatedId", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.web);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://nproom.com/");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Add the following code to enable file picking
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        registerForContextMenu(webView);
    }

    @Override
    public void onBackPressed() {
        if(webView.isFocused() && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // Override the following method to handle file picking
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = intent.getData();
            String filePath = uri.toString();
            webView.loadUrl("file://" + filePath);
        }
    }

    // Override the following method to show context menu for file picking
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.web) {
            WebView.HitTestResult result = webView.getHitTestResult();
            if (result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE ||
                    result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                menu.setHeaderTitle(result.getExtra());
                menu.add(0, FILE_PICKER_REQUEST_CODE, 0, "Pick File");
            }
        }
    }

    // Override the following method to handle context menu selection
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == FILE_PICKER_REQUEST_CODE) {
            webView.evaluateJavascript("(function() {return JSON.stringify({'src':window.getSelection().toString(),'alt':document.activeElement.alt})})();",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                String src = jsonObject.getString("src");
                                if (!TextUtils.isEmpty(src)) {
                                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                    intent.setType("*/*");
                                    startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            return true;
        }
        return super.onContextItemSelected(item);
    }
}