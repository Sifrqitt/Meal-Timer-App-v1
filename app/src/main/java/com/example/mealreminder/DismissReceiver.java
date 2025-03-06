package com.example.mealreminder;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DismissReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Meal reminder dismissed.", Toast.LENGTH_SHORT).show();

        // Cancel the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1); // Cancels the notification with ID 1
        }

        // Stop the alarm sound service
        Intent stopAlarm = new Intent(context, AlarmSoundService.class);
        context.stopService(stopAlarm);
    }
}
