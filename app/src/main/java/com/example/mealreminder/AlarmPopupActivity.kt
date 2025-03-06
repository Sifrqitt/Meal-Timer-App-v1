package com.example.mealreminder

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mealreminder.databinding.DialogAlarmPopupNewBinding

class AlarmPopupActivity : AppCompatActivity() {

    private lateinit var binding: DialogAlarmPopupNewBinding
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Correct way to use ViewBinding
        binding = DialogAlarmPopupNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Play alarm sound
        mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI).apply {
            isLooping = true
            start()
        }

        // Handle OK button click
        binding.okButton.setOnClickListener {
            stopAlarm()
            finish()
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.reset()
                it.release()
            }
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}
