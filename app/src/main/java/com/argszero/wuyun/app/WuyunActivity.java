package com.argszero.wuyun.app;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WuyunActivity extends Activity {
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wuyun);
        this.webView = ((WebView)findViewById(R.id.webView));
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.webView.requestFocus();
        this.webView.setWebViewClient(new MyWebViewClient(null));
        this.webView.setWebChromeClient(new WebChromeClient());
        this.webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        this.webView.addJavascriptInterface(new MyJavaScriptInterface(null), "backend");
        this.webView.loadUrl("file:///android_asset/wuyun.html");
        PollingUtils.startPollingService(this, 5, PollingService.class, PollingService.ACTION);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wuyun, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PollingUtils.stopPollingService(this, PollingService.class, PollingService.ACTION);
    }

    private class MyWebViewClient extends WebViewClient {
        public MyWebViewClient(Object o) {
        }
    }

    private class MyJavaScriptInterface {
        public MyJavaScriptInterface(Object o) {

        }
    }
}
