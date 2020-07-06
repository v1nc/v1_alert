package de.reckendrees.systems.alert;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.muddzdev.styleabletoast.StyleableToast;

public class HUD extends Service {

    private String message;
    @Override
    public IBinder onBind(Intent intent) {
        this.message = intent.getExtras().getString("message");
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        this.message = (String) intent.getExtras().get("message");
        serviceNotification();

        StyleableToast toast = StyleableToast.makeText(getApplicationContext(), this.message, Toast.LENGTH_LONG, R.style.alert_toast);
        /*toast.setBackground(getApplicationContext().getDrawable(R.drawable.gradient_main));
        toast.setBackgroundResource(R.drawable.gradient_main);
        toast.setBackgroundDrawable(getDrawable(R.drawable.gradient_main));*/
        toast.show();
        stopSelf();
        return START_NOT_STICKY;
    }
    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void serviceNotification(){
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "v1_alert_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "v1_Alert Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("v1_Alert")
                    .setContentText("This notification helps v1_Alert to display Toast on top of every app.").build();

            startForeground(1, notification);
        }
    }
}
