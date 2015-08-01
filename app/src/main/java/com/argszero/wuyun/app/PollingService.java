package com.argszero.wuyun.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shaoaq on 7/3/15.
 */
public class PollingService extends Service {
    public static final String ACTION = "com.argszero.wuyun.app.PollingService";

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("abc", "bind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("abc", "start command");
        new PollingThread().start();
        return super.onStartCommand(intent, flags, startId);

    }


    public static class PollingThread extends Thread {
        private static AtomicBoolean isRunning = new AtomicBoolean(false);
        private Pattern listPattern = Pattern.compile("href=\"/bugs/wooyun-([^\"#]*)\"");
        private static Pattern pagePattern= Pattern.compile("[\\s\\S]*" +
                "'wybug_title'>.{7}([^<]*)<" +
                "[\\s\\S]*" +
                "'wybug_author'>[^>]*>([^<]*)<" +
                "[\\s\\S]*" +
                "'wybug_open_date'.{8}([^<]*)<" +
                "[\\s\\S]*" +
                "'wybug_level'>[^>]*>\\s*<h3>.{7}\\s*([^<]*)<" +
                "[\\s\\S]*" +
                "");


        public PollingThread() {
        }

        @Override
        public void run() {
            if (isRunning.compareAndSet(false, true)) {
                try {
                    String lastName = Db.get().getLastName(Db.Status._public);
                    List<String> newNames = new ArrayList<String>();
                    for (int i = 1; i < 5; i++) {
                        try {
                            String html = IOUtils.toString(new URL("http://www.wooyun.org/bugs/new_public/page/" + i));
                            Log.e("abc", "i:" + i);
                            Matcher matcher = listPattern.matcher(html);
                            boolean findLastName = false;
                            while (matcher.find()) {
                                String name = matcher.group(1);
                                if (name.equals(lastName)) {
                                    findLastName = true;
                                    break;
                                }
                                newNames.add(name);
                            }
                            if (findLastName) {
                                break;
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Log.e("", "error when find new id", e);
                        }
                    }
                    Collections.reverse(newNames);
                    for (String name : newNames) {
                        try {
                            Log.e("abc", "name:" + name);
                            String html = IOUtils.toString(new URL("http://www.wooyun.org/bugs/wooyun-" + name));
                            Matcher matcher = pagePattern.matcher(html);
                            if (matcher.find()) {
                                String title = matcher.group(1);
                                String author = matcher.group(2);
                                String upTime = matcher.group(3);
                                String rank = matcher.group(4);
                                long id = Db.get().save(name, Db.Status._public, title,author,upTime,rank);
                                Db.get().saveHtml("article/id/" + id, "html.html", html);

                            }
                        } catch (Throwable e) {
                            Log.e("", "error when parse new id", e);
                        }
                    }
                } catch (Throwable e) {
                    Log.e("", "error when get new id", e);
                } finally {
                    isRunning.set(false);
                }
            }
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Service:onDestroy");
    }
}
