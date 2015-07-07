package com.argszero.wuyun.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shaoaq on 7/3/15.
 */
public class PollingService extends Service {
    public static final String ACTION = "com.argszero.wuyun.app.PollingService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Db db = new Db(this.getFilesDir().toString());
        new PollingThread(db).start();
        return super.onStartCommand(intent, flags, startId);

    }

    class PollingThread extends Thread {
        private Pattern listPattern = Pattern.compile("href=\"/bugs/wooyun-([^\"]*)\"");
        private Pattern pagePattern = Pattern.compile("[\\s\\S]*漏洞标题：([^<])*[\\s\\S]*");

        private Db db;

        public PollingThread(Db db) {
            this.db = db;
        }

        @Override
        public void run() {
            try {
                String lastId = db.getLastId();
                List<String> newIds = new ArrayList<String>();
                for (int i = 1; i < 100; i++) {
                    String html = IOUtils.toString(new URL("http://www.wooyun.org/bugs/new_public/page/" + i));
                    Matcher matcher = listPattern.matcher(html);
                    boolean findLastId = false;
                    while (matcher.find()) {
                        String id = matcher.group(1);
                        if (id.equals(lastId)) {
                            findLastId = true;
                            break;
                        }
                        newIds.add(id);
                    }
                    if (findLastId) {
                        break;
                    }
                }
                Collections.reverse(newIds);
                for (String id : newIds) {
                    String html = IOUtils.toString(new URL("http://www.wooyun.org/bugs/wooyun-" + id));

                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Service:onDestroy");
    }
}
