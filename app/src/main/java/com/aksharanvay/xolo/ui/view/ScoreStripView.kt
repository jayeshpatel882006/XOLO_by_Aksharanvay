package com.aksharanvay.xolo.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.aksharanvay.xolo.R

/**
 * Compact, animated scoreboard used by both game modes.
 */
class ScoreStripView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val palette = XoloPalette.from(context)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
        textSize = context.sp(9.5f)
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
        textSize = context.sp(25f)
    }
    private val rect = RectF()

    private var left = 0
    private var draws = 0
    private var right = 0
    private var leftLabel = "X"
    private var rightLabel = "O"
    private var drawLabel = "Draws"
    private var animatedLeft = 0f
    private var animatedDraws = 0f
    private var animatedRight = 0f
    private var animator: ValueAnimator? = null

    init {
        setWillNotDraw(false)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    fun setScores(
        left: Int,
        draws: Int,
        right: Int,
        leftLabel: String,
        rightLabel: String,
        drawLabel: String
    ) {
        val hadValues = this.left != 0 || this.draws != 0 || this.right != 0
        this.left = left
        this.draws = draws
        this.right = right
        this.leftLabel = leftLabel
        this.rightLabel = rightLabel
        this.drawLabel = drawLabel
        contentDescription = "$leftLabel $left, $drawLabel $draws, $rightLabel $right"
        if (hadValues && isAttachedToWindow) {
            animator?.cancel()
            val startLeft = animatedLeft
            val startDraws = animatedDraws
            val startRight = animatedRight
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 320L
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    val t = it.animatedValue as Float
                    animatedLeft = startLeft + (left - startLeft) * t
                    animatedDraws = startDraws + (draws - startDraws) * t
                    animatedRight = startRight + (right - startRight) * t
                    invalidate()
                }
                start()
            }
        } else {
            animatedLeft = left.toFloat()
            animatedDraws = draws.toFloat()
            animatedRight = right.toFloat()
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, context.dpInt(70))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect.set(0f, 0f, w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        val radius = context.dp(22f)
        backgroundPaint.color = palette.surface
        canvas.drawRoundRect(rect, radius, radius, backgroundPaint)
        linePaint.color = withAlpha(palette.stroke, 18)
        linePaint.strokeWidth = context.dp(1f)
        canvas.drawRoundRect(rect, radius, radius, linePaint)

        linePaint.color = withAlpha(palette.stroke, 26)
        val third = width / 3f
        canvas.drawLine(third, context.dp(16f), third, height - context.dp(16f), linePaint)
        canvas.drawLine(third * 2f, context.dp(16f), third * 2f, height - context.dp(16f), linePaint)

        val labelY = context.dp(24f)
        val valueY = context.dp(53f)
        labelPaint.textSize = context.sp(9.5f)
        valuePaint.textSize = context.sp(25f)

        drawColumn(canvas, third / 2f, labelY, valueY, leftLabel, animatedLeft, palette.solar)
        drawColumn(canvas, width / 2f, labelY, valueY, drawLabel, animatedDraws, palette.butter)
        drawColumn(canvas, third * 2.5f, labelY, valueY, rightLabel, animatedRight, palette.mint)
    }

    private fun drawColumn(
        canvas: Canvas,
        x: Float,
        labelY: Float,
        valueY: Float,
        label: String,
        value: Float,
        dotColor: Int
    ) {
        dotPaint.color = dotColor
        canvas.drawCircle(x, context.dp(10f), context.dp(2.2f), dotPaint)
        labelPaint.color = palette.textSecondary
        canvas.drawText(label.uppercase(), x, labelY, labelPaint)
        valuePaint.color = palette.textPrimary
        canvas.drawText(value.toInt().toString(), x, valueY, valuePaint)
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }
}
