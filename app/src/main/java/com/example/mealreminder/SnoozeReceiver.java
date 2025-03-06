package com.example.mealreminder;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

public class SnoozeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Meal reminder snoozed for 10 minutes!", Toast.LENGTH_SHORT).show();

        // Cancel current notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1); // Cancel notification with ID 1
        }

        // Stop the alarm sound service
        Intent stopAlarm = new Intent(context, AlarmSoundService.class);
        context.stopService(stopAlarm);

        // Reschedule the alarm for 10 minutes later
        long snoozeTimeInMillis = System.currentTimeMillis() + (10 * 60 * 1000); // 10 minutes

        Intent newIntent = new Intent(context, MealAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 1001, newIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            try {
                // Check if we can schedule exact alarms on Android 12+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(context, "Exact alarm permission required. Enable it in settings.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Set the new alarm
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTimeInMillis, pendingIntent);

            } catch (SecurityException e) {
                Toast.makeText(context, "Error: Unable to schedule exact alarm.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}
