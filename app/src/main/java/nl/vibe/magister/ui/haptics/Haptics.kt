package nl.vibe.magister.ui.haptics

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

class Haptics(private val view: View) {

    fun perform(constant: Int) = view.performHapticFeedback(constant)
}

@Composable
fun rememberHaptics(): Haptics = remember { Haptics(LocalView.current) }

fun Haptics.click() = perform(HapticFeedbackConstants.LONG_PRESS)

fun Haptics.confirm() {
    perform(
        if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM
        else HapticFeedbackConstants.LONG_PRESS
    )
}

fun Haptics.reject() {
    perform(
        if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT
        else HapticFeedbackConstants.LONG_PRESS
    )
}

fun Haptics.tick() {
    perform(
        if (Build.VERSION.SDK_INT >= 34) HapticFeedbackConstants.SEGMENT_TICK
        else HapticFeedbackConstants.VIRTUAL_KEY
    )
}
