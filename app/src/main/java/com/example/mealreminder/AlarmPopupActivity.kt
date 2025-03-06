package com.example.mealreminder

import android.app.Activity
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle

class AlarmPopupActivity : Activity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize and start playing the alarm sound
        mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI).apply {
            isLooping = true
            start()
        }

        // Show the alert dialog
        runOnUiThread {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Meal Reminder 🍽️")
                .setMessage("It's time for your meal!")
                .setPositiveButton("OK") { _, _ ->
                    stopAlarm() // Stop alarm before closing the activity
                    finish()
                }
                .setCancelable(false)
                .create()

            alertDialog.show()
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
                player.reset()  // Reset before releasing
                player.release() // Free resources
            }
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm() // Ensure alarm is stopped when activity is destroyed
    }
}
