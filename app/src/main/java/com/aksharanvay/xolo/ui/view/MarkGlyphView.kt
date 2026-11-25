package com.aksharanvay.xolo.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.annotation.ColorInt
import com.aksharanvay.xolo.R
import com.aksharanvay.xolo.game.Mark

class MarkGlyphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val palette = XoloPalette.from(context)
    private val painter = GlyphPainter()

    private var mark: Mark = Mark.X
    private var accentOverride: Int? = null
    private var scale = 1f
    private var rotation = 0f
    private var animator: ValueAnimator? = null

    init {
        isClickable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        updateContentDescription()
    }

    fun setMark(mark: Mark, animate: Boolean = false) {
        if (this.mark == mark && scale == 1f) return
        this.mark = mark
        updateContentDescription()
        if (animate) animateChange() else {
            scale = 1f
            rotation = 0f
            invalidate()
        }
    }

    fun setAccentColor(@ColorInt color: Int?) {
        accentOverride = color
        invalidate()
    }

    private fun updateContentDescription() {
        contentDescription = context.getString(
            if (mark == Mark.X) R.string.cd_mark_x else R.string.cd_mark_o
        )
    }

    fun animateChange() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 360L
            interpolator = OvershootInterpolator(1.7f)
            addUpdateListener {
                val t = it.animatedValue as Float
                scale = 0.35f + 0.65f * t
                rotation = -28f * (1f - t)
                invalidate()
            }
            start()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = when {
            MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED -> dpInt(48)
            MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED -> width
            else -> minOf(width, height)
        }
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = minOf(width, height).toFloat()
        if (size <= 0f) return
        val cx = width / 2f
        val cy = height / 2f
        val color = accentOverride ?: if (mark == Mark.X) palette.solar else palette.mint
        canvas.save()
        canvas.translate(cx, cy)
        canvas.rotate(rotation)
        canvas.scale(scale, scale)
        painter.drawMark(
            canvas = canvas,
            cx = 0f,
            cy = 0f,
            size = size,
            mark = mark,
            color = color,
            shadowColor = palette.shadow,
            shadowOffset = size * 0.045f
        )
        canvas.restore()
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }
}
