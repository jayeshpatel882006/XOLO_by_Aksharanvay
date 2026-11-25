package com.aksharanvay.xolo.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import android.widget.CompoundButton

/**
 * A custom, soft-bodied toggle. Extends CompoundButton so TalkBack and keyboard
 * users get the standard checked/not-checked semantics for free.
 */
class XoloSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CompoundButton(context, attrs, defStyleAttr) {

    private val palette = XoloPalette.from(context)
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val track = RectF()
    private val thumb = RectF()
    private var progress = 0f
    private var animator: ValueAnimator? = null

    init {
        buttonDrawable = null
        background = null
        isClickable = true
        isFocusable = true
        setWillNotDraw(false)
    }

    override fun setChecked(checked: Boolean) {
        if (isChecked == checked) return
        animateTo(if (checked) 1f else 0f)
        super.setChecked(checked)
    }

    private fun animateTo(target: Float) {
        animator?.cancel()
        if (!isAttachedToWindow) {
            progress = target
            invalidate()
            return
        }
        animator = ValueAnimator.ofFloat(progress, target).apply {
            duration = 220L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(dpInt(54), dpInt(34))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val trackH = h * 0.82f
        val trackW = w.toFloat()
        track.set(0f, (h - trackH) / 2f, trackW, (h + trackH) / 2f)
        updateThumb(w.toFloat(), h.toFloat())
    }

    private fun updateThumb(w: Float, h: Float) {
        val thumbSize = h * 0.62f
        val left = dp(4f) + progress * (w - thumbSize - dp(8f))
        thumb.set(left, (h - thumbSize) / 2f, left + thumbSize, (h + thumbSize) / 2f)
    }

    override fun onDraw(canvas: Canvas) {
        updateThumb(width.toFloat(), height.toFloat())
        val radius = track.height() / 2f
        val off = withAlpha(palette.surfaceAlt, 255)
        val on = palette.mint
        trackPaint.color = mix(off, on, progress)
        trackPaint.shader = null
        canvas.drawRoundRect(track, radius, radius, trackPaint)

        strokePaint.color = withAlpha(palette.stroke, 24)
        strokePaint.strokeWidth = dp(1f)
        canvas.drawRoundRect(track, radius, radius, strokePaint)

        // Thumb shadow then thumb.
        thumbPaint.color = withAlpha(palette.shadow, 55)
        val shadowOffset = dp(2f)
        canvas.drawCircle(
            thumb.centerX(),
            thumb.centerY() + shadowOffset,
            thumb.width() / 2f,
            thumbPaint
        )
        thumbPaint.color = palette.surface
        canvas.drawCircle(thumb.centerX(), thumb.centerY(), thumb.width() / 2f, thumbPaint)

        thumbPaint.color = withAlpha(palette.mintDeep, (90 * progress).toInt())
        canvas.drawCircle(
            thumb.centerX(),
            thumb.centerY(),
            thumb.width() * 0.18f,
            thumbPaint
        )
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }
}
