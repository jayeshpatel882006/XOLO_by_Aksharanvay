package com.aksharanvay.xolo.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.aksharanvay.xolo.R
import kotlin.math.PI
import kotlin.math.sin

/**
 * Three breathing dots. It only animates while it is visible, so a resting game
 * costs nothing.
 */
class ThinkingIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val palette = XoloPalette.from(context)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var phase = 0f
    private var animator: ValueAnimator? = null
    private var active = false

    init {
        contentDescription = context.getString(R.string.cd_thinking)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        setWillNotDraw(false)
    }

    fun setActive(active: Boolean) {
        if (this.active == active) return
        this.active = active
        if (active && isAttachedToWindow) startAnimation() else stopAnimation()
        invalidate()
    }

    private fun startAnimation() {
        if (animator != null) return
        animator = ValueAnimator.ofFloat(0f, (PI * 2).toFloat()).apply {
            duration = 1050L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                phase = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun stopAnimation() {
        animator?.cancel()
        animator = null
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(context.dpInt(52), context.dpInt(28))
    }

    override fun onDraw(canvas: Canvas) {
        if (!active) return
        val centerY = height / 2f
        val colors = intArrayOf(palette.solar, palette.butter, palette.mint)
        val gap = context.dp(12f)
        val startX = width / 2f - gap
        for (i in 0..2) {
            val wave = sin(phase + i * 0.9f)
            val alpha = (150 + 105 * ((wave + 1f) / 2f)).toInt()
            val y = centerY + wave * context.dp(2.5f)
            paint.color = withAlpha(colors[i], alpha)
            canvas.drawCircle(startX + i * gap, y, context.dp(3.4f), paint)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (active) startAnimation()
    }

    override fun onDetachedFromWindow() {
        stopAnimation()
        super.onDetachedFromWindow()
    }
}
