package com.example.mealreminder

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.widget.RemoteViews
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mealTimePickerButton: Button
    private lateinit var mealTimeDisplay: TextView
    private lateinit var mealSchedule: TextView
    private lateinit var setMealButton: Button
    private lateinit var resetButton: Button
    private lateinit var alarmManager: AlarmManager
    private var selectedHour: Int = -1
    private var selectedMinute: Int = -1
    private val mealIntents = arrayOfNulls<PendingIntent>(4) // ✅ Correct size (supports 4 alarms)

    companion object {
        const val CHANNEL_ID = "MealReminderChannel"
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestExactAlarmPermission() // Ask for exact alarm permission (Android 12+)
        requestNotificationPermission() // Ask for notification permission (Android 13+)

        mealTimePickerButton = findViewById(R.id.mealTimePickerButton)
        mealTimeDisplay = findViewById(R.id.mealTimeDisplay)
        mealSchedule = findViewById(R.id.mealSchedule)
        setMealButton = findViewById(R.id.setMealButton)
        resetButton = findViewById(R.id.resetButton)
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        createNotificationChannel()

        mealTimePickerButton.setOnClickListener { showTimePicker() }
        setMealButton.setOnClickListener { setMealSchedule() }
        resetButton.setOnClickListener { resetSchedule() }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                this.selectedHour = selectedHour
                this.selectedMinute = selectedMinute
                val amPm = if (selectedHour < 12) "AM" else "PM"
                val formattedHour = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                mealTimeDisplay.text = "Selected Time: $formattedHour:${String.format("%02d", selectedMinute)} $amPm"
            },
            hour,
            minute,
            false // 12-hour format with AM/PM
        )

        timePickerDialog.show()
    }

    private fun setMealSchedule() {
        if (selectedHour == -1 || selectedMinute == -1) {
            mealSchedule.text = "Please select a meal time first."
            return
        }

        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
        calendar.set(Calendar.MINUTE, selectedMinute)
        calendar.set(Calendar.SECOND, 0)

        val scheduleText = StringBuilder("Meal Schedule:\n")

        for (i in 0..3) { // Ensure exactly 4 meal times
            val amPm = if (calendar.get(Calendar.HOUR_OF_DAY) < 12) "AM" else "PM"
            val formattedHour = if (calendar.get(Calendar.HOUR_OF_DAY) % 12 == 0) 12 else calendar.get(Calendar.HOUR_OF_DAY) % 12

            if (calendar.after(now)) { // Only schedule if the time is in the future
                scheduleText.append("Meal ${i + 1}: $formattedHour:${String.format("%02d", calendar.get(Calendar.MINUTE))} $amPm\n")
                setMealAlarm(i, calendar.timeInMillis)
            } else {
                scheduleText.append("Meal ${i + 1}: Skipped (Past time)\n")
            }

            // Move to the next meal time (3 hours later)
            calendar.add(Calendar.HOUR_OF_DAY, 3)
        }

        mealSchedule.text = scheduleText.toString()
    }

    private fun setMealAlarm(index: Int, timeInMillis: Long) {
        val intent = Intent(this, MealAlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            index,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mealIntents[index] = pendingIntent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                mealSchedule.text = "Permission required for exact alarms. Please enable in settings."
                return
            }
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        showCustomNotification() // Show sleek notification when meal is set
    }

    private fun resetSchedule() {
        for (i in 0..2) {
            mealIntents[i]?.let { alarmManager.cancel(it) }
        }
        mealSchedule.text = "Schedule reset."
        mealTimeDisplay.text = "No time selected"
        selectedHour = -1
        selectedMinute = -1
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Meal Reminder"
            val descriptionText = "Notifies meal times"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showCustomNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return // Don't show notification if permission is not granted
        }

        val notificationLayout = RemoteViews(packageName, R.layout.custom_notification)

        // Intent for Snooze action
        val snoozeIntent = Intent(this, SnoozeReceiver::class.java)
        val snoozePendingIntent = PendingIntent.getBroadcast(this, 1001, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        notificationLayout.setOnClickPendingIntent(R.id.snooze_button, snoozePendingIntent)

        // Intent for Dismiss action
        val dismissIntent = Intent(this, DismissReceiver::class.java)
        val dismissPendingIntent = PendingIntent.getBroadcast(this, 1002, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        notificationLayout.setOnClickPendingIntent(R.id.dismiss_button, dismissPendingIntent)

        // Start alarm sound service
        val alarmIntent = Intent(this, AlarmSoundService::class.java)
        startService(alarmIntent)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_meal)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)  // Makes the notification persistent
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(dismissPendingIntent, true)  // Makes it high priority

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }
}
