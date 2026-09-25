package com.pixelfitquest.feature.workoutResume.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pixelfitquest.feature.workoutResume.model.CoachingVisuals
import com.pixelfitquest.ui.theme.HeartRuby
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.TorchAmber
import com.pixelfitquest.ui.theme.VitalGreen
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 16-bit SNES Pixel Art Canvas Visual for bench press form mistakes & reference posture.
 * Renders dynamically with pixel-grid styling and 6-frame loop animations.
 */
@Composable
fun FormClipVisual(
    tag: String,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    val transition = rememberInfiniteTransition(label = "formClipLoop")

    // 6-frame 8 fps loop cycle (750ms total loop)
    val loopPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "framePhase",
    )

    val frame = loopPhase.toInt().coerceIn(0, 5)

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val pixel = (this.size.width / 64f).coerceAtLeast(1f)

            // Bench pad
            drawRect(
                color = SlateBorder,
                topLeft = Offset(24 * pixel, 28 * pixel),
                size = Size(16 * pixel, 34 * pixel),
            )
            drawRect(
                color = SlateDeep,
                topLeft = Offset(25 * pixel, 29 * pixel),
                size = Size(14 * pixel, 32 * pixel),
            )

            // Lifter Head & Neck
            drawRect(
                color = Color(0xFF14181F),
                topLeft = Offset(27 * pixel, 19 * pixel),
                size = Size(10 * pixel, 12 * pixel),
            )
            drawRect(
                color = Color(0xFFE89878), // Peach skin
                topLeft = Offset(28 * pixel, 20 * pixel),
                size = Size(8 * pixel, 10 * pixel),
            )

            // Torso & Black Singlet
            drawRect(
                color = Color(0xFF14181F),
                topLeft = Offset(22 * pixel, 30 * pixel),
                size = Size(20 * pixel, 22 * pixel),
            )
            drawRect(
                color = Color(0xFF1E1E24), // Black singlet body
                topLeft = Offset(24 * pixel, 31 * pixel),
                size = Size(16 * pixel, 20 * pixel),
            )

            // Legs on sides of bench
            drawRect(
                color = Color(0xFF14181F),
                topLeft = Offset(16 * pixel, 40 * pixel),
                size = Size(7 * pixel, 20 * pixel),
            )
            drawRect(
                color = Color(0xFFE89878),
                topLeft = Offset(17 * pixel, 41 * pixel),
                size = Size(5 * pixel, 18 * pixel),
            )

            drawRect(
                color = Color(0xFF14181F),
                topLeft = Offset(41 * pixel, 40 * pixel),
                size = Size(7 * pixel, 20 * pixel),
            )
            drawRect(
                color = Color(0xFFE89878),
                topLeft = Offset(42 * pixel, 41 * pixel),
                size = Size(5 * pixel, 18 * pixel),
            )

            // Draw arms and barbell based on coaching tag
            when (tag) {
                CoachingVisuals.TAG_TILT_RIGHT -> {
                    drawTiltMistake(pixel = pixel, frame = frame, isRightHigh = true)
                }
                CoachingVisuals.TAG_TILT_LEFT -> {
                    drawTiltMistake(pixel = pixel, frame = frame, isRightHigh = false)
                }
                CoachingVisuals.TAG_TWIST_HEAD -> {
                    drawTwistMistake(pixel = pixel, frame = frame, isHeadForwardRight = true)
                }
                CoachingVisuals.TAG_TWIST_HIP -> {
                    drawTwistMistake(pixel = pixel, frame = frame, isHeadForwardRight = false)
                }
                CoachingVisuals.TAG_SHORT_ROM -> {
                    drawShortRomMistake(pixel = pixel, frame = frame)
                }
                CoachingVisuals.TAG_DROPPED -> {
                    drawDroppedMistake(pixel = pixel, frame = frame)
                }
                "clip_pose", "clip_pose_unclear" -> {
                    drawClipPoseMistake(pixel = pixel, frame = frame)
                }
                else -> {
                    drawFormEven(pixel = pixel, frame = frame)
                }
            }
        }
    }
}

/** Tilt mistake: One side of the barbell is elevated higher with red outline */
private fun DrawScope.drawTiltMistake(pixel: Float, frame: Int, isRightHigh: Boolean) {
    // 6-frame loop: tilt angle grows, holds, eases back
    val wobble = when (frame) {
        0 -> 11f
        1 -> 14f
        2 -> 17f
        3 -> 17f
        4 -> 14f
        else -> 12f
    }
    val angle = if (isRightHigh) wobble else -wobble

    // Arms
    val leftArmY = if (isRightHigh) 20 * pixel else 13 * pixel
    val rightArmY = if (isRightHigh) 13 * pixel else 20 * pixel

    // Left arm
    drawArm(pixel = pixel, shoulder = Offset(22 * pixel, 32 * pixel), hand = Offset(17 * pixel, leftArmY))
    // Right arm with phone sleeve
    drawArm(pixel = pixel, shoulder = Offset(42 * pixel, 32 * pixel), hand = Offset(47 * pixel, rightArmY), hasPhone = true)

    // Barbell rotated
    rotate(degrees = angle, pivot = Offset(32 * pixel, 17 * pixel)) {
        drawBarbell(pixel = pixel)
        // Red accent highlight on the high side of the bar
        val accentX = if (isRightHigh) 36 * pixel else 8 * pixel
        drawRect(
            color = HeartRuby,
            topLeft = Offset(accentX, 14 * pixel),
            size = Size(20 * pixel, 6 * pixel),
            style = Stroke(width = 2 * pixel),
        )
    }
}

/** Twist mistake: Horizontal plane yaw skew with one hand closer to head */
private fun DrawScope.drawTwistMistake(pixel: Float, frame: Int, isHeadForwardRight: Boolean) {
    val yawOffset = when (frame) {
        0 -> 2f
        1 -> 4f
        2 -> 5f
        3 -> 5f
        4 -> 3f
        else -> 1f
    } * if (isHeadForwardRight) 1f else -1f

    val rightHandY = 16 * pixel - (yawOffset * pixel)
    val leftHandY = 16 * pixel + (yawOffset * pixel)

    drawArm(pixel = pixel, shoulder = Offset(22 * pixel, 32 * pixel), hand = Offset(17 * pixel, leftHandY))
    drawArm(pixel = pixel, shoulder = Offset(42 * pixel, 32 * pixel), hand = Offset(47 * pixel, rightHandY), hasPhone = true)

    // Bar stays level in height, but one sleeve drawn thicker (closer to viewer/head)
    drawBarbell(pixel = pixel)

    // Red accent on the skewed forward end
    val accentX = if (isHeadForwardRight) 40 * pixel else 6 * pixel
    drawCircle(
        color = TorchAmber,
        radius = 5 * pixel,
        center = Offset(accentX + 6 * pixel, 17 * pixel),
        style = Stroke(width = 2 * pixel),
    )
}

/** Short ROM: bar bobs above chest; ghost bar at chest with red gap bracket */
private fun DrawScope.drawShortRomMistake(pixel: Float, frame: Int) {
    // Bobbing bar in upper range
    val bobY = when (frame) {
        0 -> 12 * pixel
        1 -> 14 * pixel
        2 -> 16 * pixel
        3 -> 17 * pixel
        4 -> 15 * pixel
        else -> 13 * pixel
    }

    // Ghost barbell touching chest (at 26 * pixel)
    drawRect(
        color = Color(0x558CA0B4),
        topLeft = Offset(8 * pixel, 25 * pixel),
        size = Size(48 * pixel, 3 * pixel),
    )
    drawRect(
        color = Color(0x558CA0B4),
        topLeft = Offset(6 * pixel, 21 * pixel),
        size = Size(4 * pixel, 11 * pixel),
    )
    drawRect(
        color = Color(0x558CA0B4),
        topLeft = Offset(54 * pixel, 21 * pixel),
        size = Size(4 * pixel, 11 * pixel),
    )

    // Real bar bouncing high
    drawArm(pixel = pixel, shoulder = Offset(22 * pixel, 32 * pixel), hand = Offset(17 * pixel, bobY + 1 * pixel))
    drawArm(pixel = pixel, shoulder = Offset(42 * pixel, 32 * pixel), hand = Offset(47 * pixel, bobY + 1 * pixel), hasPhone = true)

    drawBarbellAt(pixel = pixel, y = bobY)

    // Red bracket indicating shallow ROM gap
    drawLine(
        color = TorchAmber,
        start = Offset(32 * pixel, bobY + 4 * pixel),
        end = Offset(32 * pixel, 24 * pixel),
        strokeWidth = 2 * pixel,
    )
    drawRect(
        color = TorchAmber,
        topLeft = Offset(29 * pixel, 23 * pixel),
        size = Size(6 * pixel, 2 * pixel),
    )
}

/** Dropped mistake: Rapid 2-frame fall onto chest, 4-frame slow push with red motion trails */
private fun DrawScope.drawDroppedMistake(pixel: Float, frame: Int) {
    // 0, 1 = fast drop; 2, 3, 4, 5 = slow concentric push
    val dropY = when (frame) {
        0 -> 10 * pixel
        1 -> 25 * pixel // Slam on chest in 1 step!
        2 -> 23 * pixel
        3 -> 19 * pixel
        4 -> 15 * pixel
        else -> 11 * pixel
    }

    drawArm(pixel = pixel, shoulder = Offset(22 * pixel, 32 * pixel), hand = Offset(17 * pixel, dropY))
    drawArm(pixel = pixel, shoulder = Offset(42 * pixel, 32 * pixel), hand = Offset(47 * pixel, dropY), hasPhone = true)

    drawBarbellAt(pixel = pixel, y = dropY)

    // Red speed streaks only on frame 0 and 1
    if (frame <= 1) {
        drawLine(
            color = HeartRuby,
            start = Offset(14 * pixel, 10 * pixel),
            end = Offset(14 * pixel, 22 * pixel),
            strokeWidth = 2 * pixel,
        )
        drawLine(
            color = HeartRuby,
            start = Offset(50 * pixel, 10 * pixel),
            end = Offset(50 * pixel, 22 * pixel),
            strokeWidth = 2 * pixel,
        )
    }
}

/** Clip pose mistake: phone misaligned with flashing question mark */
private fun DrawScope.drawClipPoseMistake(pixel: Float, frame: Int) {
    val barY = 16 * pixel
    drawArm(pixel = pixel, shoulder = Offset(22 * pixel, 32 * pixel), hand = Offset(17 * pixel, barY))
    // Right arm with loose wiggling phone
    val phoneWiggle = if (frame % 2 == 0) -2 * pixel else 2 * pixel
    drawArm(pixel = pixel, shoulder = Offset(42 * pixel, 32 * pixel), hand = Offset(47 * pixel, barY), hasPhone = true, phoneOffsetX = phoneWiggle)

    drawBarbellAt(pixel = pixel, y = barY)

    // Flashing question mark on right sleeve phone
    if (frame % 3 != 0) {
        drawCircle(
            color = TorchAmber,
            radius = 3 * pixel,
            center = Offset(49 * pixel + phoneWiggle, 20 * pixel),
        )
    }
}

/** Form even: perfectly level bar, symmetrical lockout, clean steel */
private fun DrawScope.drawFormEven(pixel: Float, frame: Int) {
    // Subtle breathing lockout
    val breathY = if (frame % 2 == 0) 15 * pixel else 16 * pixel
    drawArm(pixel = pixel, shoulder = Offset(22 * pixel, 32 * pixel), hand = Offset(17 * pixel, breathY))
    drawArm(pixel = pixel, shoulder = Offset(42 * pixel, 32 * pixel), hand = Offset(47 * pixel, breathY), hasPhone = true)

    drawBarbellAt(pixel = pixel, y = breathY)

    // Clean green sparkle / balance marker
    drawRect(
        color = VitalGreen,
        topLeft = Offset(31 * pixel, breathY - 3 * pixel),
        size = Size(2 * pixel, 2 * pixel),
    )
}

private fun DrawScope.drawArm(
    pixel: Float,
    shoulder: Offset,
    hand: Offset,
    hasPhone: Boolean = false,
    phoneOffsetX: Float = 0f,
) {
    // Arm outline & flesh
    drawLine(
        color = Color(0xFF14181F),
        start = shoulder,
        end = hand,
        strokeWidth = 6 * pixel,
    )
    drawLine(
        color = Color(0xFFE89878),
        start = shoulder,
        end = hand,
        strokeWidth = 4 * pixel,
    )

    // Phone sleeve band
    if (hasPhone) {
        val midX = (shoulder.x + hand.x) / 2f + phoneOffsetX
        val midY = (shoulder.y + hand.y) / 2f
        drawRect(
            color = Color(0xFF243040),
            topLeft = Offset(midX - 3 * pixel, midY - 3 * pixel),
            size = Size(6 * pixel, 6 * pixel),
        )
        drawRect(
            color = Color(0xFF70E0F8), // Screen glow
            topLeft = Offset(midX - 2 * pixel, midY - 2 * pixel),
            size = Size(4 * pixel, 4 * pixel),
        )
    }
}

private fun DrawScope.drawBarbell(pixel: Float) {
    drawBarbellAt(pixel, 16 * pixel)
}

private fun DrawScope.drawBarbellAt(pixel: Float, y: Float) {
    // Steel bar shaft
    drawRect(
        color = Color(0xFF14181F),
        topLeft = Offset(6 * pixel, y - 2 * pixel),
        size = Size(52 * pixel, 5 * pixel),
    )
    drawRect(
        color = SilverSteel,
        topLeft = Offset(7 * pixel, y - 1 * pixel),
        size = Size(50 * pixel, 3 * pixel),
    )

    // Left Plates
    drawRect(
        color = Color(0xFF14181F),
        topLeft = Offset(5 * pixel, y - 9 * pixel),
        size = Size(6 * pixel, 19 * pixel),
    )
    drawRect(
        color = SilverSlate,
        topLeft = Offset(6 * pixel, y - 8 * pixel),
        size = Size(4 * pixel, 17 * pixel),
    )

    // Right Plates
    drawRect(
        color = Color(0xFF14181F),
        topLeft = Offset(53 * pixel, y - 9 * pixel),
        size = Size(6 * pixel, 19 * pixel),
    )
    drawRect(
        color = SilverSlate,
        topLeft = Offset(54 * pixel, y - 8 * pixel),
        size = Size(4 * pixel, 17 * pixel),
    )
}
