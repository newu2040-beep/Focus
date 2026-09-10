package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticHelper(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playClickHaptic(enabled: Boolean = true) {
        if (!enabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20L)
            }
        } catch (_: Exception) {}
    }

    fun playCompletionFeedback(soundEnabled: Boolean = true, vibrationEnabled: Boolean = true) {
        if (vibrationEnabled && vibrator?.hasVibrator() == true) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 150, 100, 250)
                    val amplitudes = intArrayOf(0, 180, 0, 255)
                    vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(400L)
                }
            } catch (_: Exception) {}
        }

        if (soundEnabled) {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
            } catch (_: Exception) {}
        }
    }
}
