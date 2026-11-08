package nl.vibe.magister.ui.haptics

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

fun HapticFeedback.click() = performHapticFeedback(HapticFeedbackType.LongPress)

fun HapticFeedback.confirm() = performHapticFeedback(HapticFeedbackType.Confirm)

fun HapticFeedback.reject() = performHapticFeedback(HapticFeedbackType.Reject)

fun HapticFeedback.tick() = performHapticFeedback(HapticFeedbackType.SegmentTick)
