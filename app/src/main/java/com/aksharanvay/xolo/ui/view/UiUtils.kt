package com.aksharanvay.xolo.ui.view

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import kotlin.math.roundToInt

fun Context.dp(value: Float): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    value,
    resources.displayMetrics
)

fun Context.dpInt(value: Int): Int = (dp(value.toFloat()) + 0.5f).roundToInt()

fun Context.sp(value: Float): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP,
    value,
    resources.displayMetrics
)

fun View.dp(value: Float): Float = context.dp(value)

fun View.dpInt(value: Int): Int = context.dpInt(value)

fun View.sp(value: Float): Float = context.sp(value)

@ColorInt
fun withAlpha(@ColorInt color: Int, alpha: Int): Int =
    (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)

@ColorInt
fun mix(@ColorInt from: Int, @ColorInt to: Int, fraction: Float): Int {
    val t = fraction.coerceIn(0f, 1f)
    val a = (Color.alpha(from) + (Color.alpha(to) - Color.alpha(from)) * t).roundToInt()
    val r = (Color.red(from) + (Color.red(to) - Color.red(from)) * t).roundToInt()
    val g = (Color.green(from) + (Color.green(to) - Color.green(from)) * t).roundToInt()
    val b = (Color.blue(from) + (Color.blue(to) - Color.blue(from)) * t).roundToInt()
    return Color.argb(a, r, g, b)
}

@ColorInt
fun lighten(@ColorInt color: Int, amount: Float): Int = mix(color, Color.WHITE, amount)

@ColorInt
fun darken(@ColorInt color: Int, amount: Float): Int = mix(color, Color.BLACK, amount)

/**
 * A tiny press response shared by icon buttons and text actions.
 */
fun View.tactilePress(scale: Float = 0.9f, pressAlpha: Float = 0.82f) {
    setOnTouchListener { view, event ->
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                view.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .alpha(pressAlpha)
                    .setDuration(90L)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            android.view.MotionEvent.ACTION_UP,
            android.view.MotionEvent.ACTION_CANCEL -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(150L)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        }
        false
    }
}
