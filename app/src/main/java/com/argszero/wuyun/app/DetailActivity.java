package com.argszero.wuyun.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.nio.charset.Charset;


public class DetailActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        this.webView = ((WebView) findViewById(R.id.webView));
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(this, "backend");
        this.webView.setWebChromeClient(new WebChromeClient() {
        });
        this.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.webView.requestFocus();
        this.webView.setWebViewClient(new MyWebViewClient());
        this.webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        int id = getIntent().getIntExtra("articleId", 0);
        this.webView.getSettings().setDefaultTextEncodingName("utf-8");
        try {
            String html = Db.get().readHtml("article/id/" + id, "html.html");
//            this.webView.loadDataWithBaseURL(Db.get().getBaseUrl("article/id/" + id),html, "text/html", "utf-8",null);
            this.webView.loadUrl(Db.get().getBaseUrl("article/id/" + id)+"/html.html");
        } catch (IOException e) {
            this.webView.loadData("已删除", "text/html; charset=UTF-8", null);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            if (this.webView.canGoBack()) {
                this.webView.goBack();
                return true;
            }else{
                finish();
                return true;
            }
        }
        return false;
    }

    private class MyWebViewClient extends WebViewClient {
    }
}
