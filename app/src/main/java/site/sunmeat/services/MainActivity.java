package site.sunmeat.services;

import android.app.ActivityManager;
import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import com.permissionx.guolindev.PermissionX;

public class MainActivity extends AppCompatActivity {
    // запит дозволу на показ сповіщень (з використанням бібліотеки permissionx)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkNotificationsPermission();
    }

    // перевірка та запит дозволу на показ сповіщень
    private void checkNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionX.init(this)
                    .permissions(Manifest.permission.POST_NOTIFICATIONS)
                    .request((allGranted, grantedList, deniedList) -> {
                        if (allGranted) {
                            showToast("дозвіл на показ сповіщень отримано");
                        } else {
                            showToast("дозвіл на показ сповіщень не отримано");
                        }
                    });
        }
    }

    // виведення тоста
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void startImageService(View v) {
        // перевірка, чи вже запущено сервіс
        if (isServiceRunning(ImageChangerService.class)) {
            showToast("сервіс уже запущено");
            return;
        }
        var i = new Intent(this, ImageChangerService.class);
        i.setAction("start");
        ContextCompat.startForegroundService(this, i);
        showToast("сервіс запущено");
    }

    public void stopImageService(View v) {
        // перевірка, чи сервіс запущено перед зупинкою
        if (!isServiceRunning(ImageChangerService.class)) {
            showToast("сервіс не запущено");
            return;
        }
        var i = new Intent(this, ImageChangerService.class);
        i.setAction("stop");
        startService(i); // для зупинки можна залишити startService, він передасть intent в onStartCommand
        showToast("сервіс зупинено");
    }

    // перевірка, чи запущено сервіс
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        // для зворотної сумісності, хоч getRunningServices і deprecated, але для власного сервісу спрацює :)
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}