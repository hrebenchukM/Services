package site.sunmeat.services;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.os.*;
import android.util.Log;

import java.util.*;

public class AppMonitorService extends Service {

    final String LOG_TAG = "monitor_service";

    Handler h;
    Runnable r;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "AppMonitorService onCreate");

        h = new Handler(Looper.getMainLooper());
        r = new Runnable() {
            @Override
            public void run() {

                String top = getTopPackage();

                Log.i(LOG_TAG, "top = " + top);

                if (top != null && !top.equals(getPackageName())) {

                    Intent i = new Intent(AppMonitorService.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);

                    Log.i(LOG_TAG, "Bring MainActivity to front");
                }

                h.postDelayed(this, 2000);
            }
        };

        h.post(r);
    }

    private String getTopPackage() {
        long end = System.currentTimeMillis();
        long start = end - 2000;

        UsageStatsManager usm = (UsageStatsManager)
                getSystemService(USAGE_STATS_SERVICE);

        List<UsageStats> stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, start, end
        );

        if (stats == null || stats.isEmpty())
            return null;

        UsageStats recent = null;

        for (UsageStats s : stats) {
            if (recent == null || s.getLastTimeUsed() > recent.getLastTimeUsed()) {
                recent = s;
            }
        }

        return recent != null ? recent.getPackageName() : null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "AppMonitorService onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "AppMonitorService onDestroy");
        h.removeCallbacks(r);
        super.onDestroy();
    }
}
