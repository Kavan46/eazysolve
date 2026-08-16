package com.example.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object HapticManager {
    var hapticsEnabled: Boolean = true

    private var vibrator: Vibrator? = null

    fun init(context: Context) {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playTap(hapticFeedback: HapticFeedback? = null) {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (vibrator?.hasVibrator() == true) {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            } else {
                hapticFeedback?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } catch (_: Exception) {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun playLightTap(hapticFeedback: HapticFeedback? = null) {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (vibrator?.hasVibrator() == true) {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10)
            } else {
                hapticFeedback?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } catch (_: Exception) {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun playHeavyClick(hapticFeedback: HapticFeedback? = null) {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (vibrator?.hasVibrator() == true) {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            } else {
                hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (_: Exception) {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun playSuccess(hapticFeedback: HapticFeedback? = null) {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator?.hasVibrator() == true) {
                val timings = longArrayOf(0, 30, 60, 40)
                val amplitudes = intArrayOf(0, 180, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else if (vibrator?.hasVibrator() == true) {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(60)
            } else {
                hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (_: Exception) {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun playError(hapticFeedback: HapticFeedback? = null) {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator?.hasVibrator() == true) {
                val timings = longArrayOf(0, 40, 40, 40)
                val amplitudes = intArrayOf(0, 220, 0, 220)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else if (vibrator?.hasVibrator() == true) {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            } else {
                hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (_: Exception) {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}
