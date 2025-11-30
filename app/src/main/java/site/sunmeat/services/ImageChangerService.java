package site.sunmeat.services;

import android.app.*;
import android.content.*;
import android.graphics.BitmapFactory;
import android.os.*;
import androidx.core.app.NotificationCompat;
import android.app.WallpaperManager;

public class ImageChangerService  extends Service {
    private Handler h;
    private Runnable r;

    int counter = 0;

    int[] images = {
            R.drawable.wall_1,
            R.drawable.wall_2,
            R.drawable.wall_3
    };

    private static final String CHANNEL_ID = "alex_channel";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification updateNotification() {
        counter++;
        var info = "Змінено шпалери: " + counter;
        var manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Context context = getApplicationContext();

        createNotificationChannel(manager);
        var builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        PendingIntent action = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE
        );

        builder.setContentIntent(action)
                .setContentTitle(info)
                .setTicker(info)
                .setContentText(info)
                .setSmallIcon(R.drawable.barcode)
                .setOngoing(true)
                .build();

        return builder.build();
    }

    private void createNotificationChannel(NotificationManager manager) {
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            var channel = new NotificationChannel(
                    CHANNEL_ID,
                    "AlexChannel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("My notification channel description");
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null && intent.getAction().contains("start")) {

            Notification initialNotification = updateNotification();
            startForeground(101, initialNotification);

            h = new Handler(Looper.getMainLooper());
            r = new Runnable() {
                int index = 0;

                @Override
                public void run() {


                    try {
                        var wm = WallpaperManager.getInstance(ImageChangerService.this);
                        var bmp = BitmapFactory.decodeResource(getResources(), images[index]);
                        wm.setBitmap(bmp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // оновити нотифікацію
                    startForeground(101, updateNotification());

                    // перейти до наступної картинки
                    index = (index + 1) % images.length;


                    h.postDelayed(this, 3000);
                }
            };
            h.post(r);

        } else if (intent != null && intent.getAction() != null && intent.getAction().contains("stop")) {

            if (h != null && r != null) {
                h.removeCallbacks(r);
            }
            stopForeground(STOP_FOREGROUND_DETACH);
            stopSelf();
        }

        return Service.START_STICKY;
    }
}