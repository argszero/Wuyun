package com.argszero.wuyun.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;


public class WuyunActivity extends Activity {
    private WebView webView;
    private long lastExitTime = 0L;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        File dir = this.getExternalFilesDir(null);
        if (dir == null) {
            dir = this.getFilesDir();
        }
        Db.get().init(dir.toString());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wuyun);
        this.webView = ((WebView) findViewById(R.id.webView));
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new MyJavaScriptInterface(this), "backend");
        this.webView.setWebChromeClient(new WebChromeClient() {
        });
        this.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.webView.requestFocus();
        this.webView.setWebViewClient(new MyWebViewClient(null));
        this.webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        this.webView.loadUrl("file:///android_asset/index.html");
        new PollingService.PollingThread().start();
        PollingUtils.startPollingService(this, 600, PollingService.class, PollingService.ACTION);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wuyun, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        Log.e("abc", "destroy");
        super.onDestroy();
//        PollingUtils.stopPollingService(this, PollingService.class, PollingService.ACTION);
//        Db.close();
    }

    private class MyWebViewClient extends WebViewClient {
        public MyWebViewClient(Object o) {
        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == 4) {
//            if (this.webView.canGoBack()) {
//                this.webView.goBack();
//                return true;
//            }
//            if (System.currentTimeMillis() - this.lastExitTime > 3000L) {
//                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
//                this.lastExitTime = System.currentTimeMillis();
//            } else {
//                finish();
//                System.exit(0);
//            }
//
//        }
//        return false;
//    }
}
