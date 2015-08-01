package com.argszero.wuyun.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Created by shaoaq on 7/9/15.
 */
public class MyJavaScriptInterface {

    private WuyunActivity wuyunActivity;

    public MyJavaScriptInterface(WuyunActivity wuyunActivity) {

        this.wuyunActivity = wuyunActivity;
    }

    @JavascriptInterface
    public String listPublic() {
        return Db.get().list(Db.Status._public).toString();
    }

    @JavascriptInterface
    public int getPublicCurrentItemId() {
        final SharedPreferences sharedPreferences = wuyunActivity.getSharedPreferences("wuyun", Activity.MODE_PRIVATE);
        int publicCurrentItemId = sharedPreferences.getInt("PublicCurrentItemId", 15);
        return publicCurrentItemId;
    }

    @JavascriptInterface
    public void setPublicCurrentItemId(int itemId) {
        final SharedPreferences sharedPreferences = wuyunActivity.getSharedPreferences("wuyun", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("PublicCurrentItemId", itemId);
        editor.commit();
    }

    @JavascriptInterface
    public String listPublicBeforeInclude(int itemId, int limit) {
        return Db.get().listBeforeInclude(Db.Status._public, itemId, limit).toString();
    }

    @JavascriptInterface
    public String listPublicAfter(int itemId, int limit) {
        return Db.get().listAfter(Db.Status._public, itemId, limit).toString();
    }

    @JavascriptInterface
    public String listPublicBefore(int itemId, int limit) {
        return Db.get().listBefore(Db.Status._public, itemId, limit).toString();
    }

    @JavascriptInterface
    public void openDetail(int articleId) {
        Intent localIntent = new Intent();
        localIntent.putExtra("articleId", articleId);
        localIntent.setClass(wuyunActivity.getApplicationContext(), DetailActivity.class);
        wuyunActivity.startActivity(localIntent);
    }

    @JavascriptInterface
    public int countArticle() {
        int count = Db.get().countArticle();
        Log.i("abc","count:"+count);
        return count;
    }
}
