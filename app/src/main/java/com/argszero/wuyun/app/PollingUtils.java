package com.argszero.wuyun.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by shaoaq on 7/3/15.
 */
public class PollingUtils {
    private static boolean isStarted=false;
    public static void startPollingService(Context context, int seconds, Class<?> cls,String action) {
        if(!isStarted){
            AlarmManager manager = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, cls);
            intent.setAction(action);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            long triggerAtTime = SystemClock.elapsedRealtime();

            Log.e("abc","set alarm");
            manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,
                    seconds * 1000, pendingIntent);
            Log.e("abc","set alarm end");
            isStarted=true;
        }
    }
    //停止轮询服务
    public static void stopPollingService(Context context, Class<?> cls,String action) {
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //取消正在执行的服务
        manager.cancel(pendingIntent);
    }
}
