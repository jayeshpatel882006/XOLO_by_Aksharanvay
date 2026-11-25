package com.aksharanvay.xolo.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import com.aksharanvay.xolo.R

/**
 * A soft segmented pill used for theme and difficulty choices.
 */
class SegmentedControl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val palette = XoloPalette.from(context)
    private val containerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val container = RectF()
    private val pill = RectF()
    private var options: List<String> = emptyList()
    private var selectedIndex = 0
    private var animatedIndex = 0f
    private var animator: ValueAnimator? = null

    @ColorInt private var accent: Int = palette.mint

    var onSelected: ((Int) -> Unit)? = null

    init {
        isClickable = true
        isFocusable = true
        setWillNotDraw(false)
    }

    fun setOptions(options: List<String>, selected: Int = 0) {
        this.options = options
        selectedIndex = selected.coerceIn(0, (options.size - 1).coerceAtLeast(0))
        animatedIndex = selectedIndex.toFloat()
        updateDescription()
        invalidate()
    }

    fun setSelected(index: Int, notify: Boolean = false) {
        val target = index.coerceIn(0, (options.size - 1).coerceAtLeast(0))
        if (target == selectedIndex) return
        animateTo(target)
        selectedIndex = target
        updateDescription()
        if (notify) onSelected?.invoke(target)
    }

    fun setAccent(@ColorInt color: Int) {
        accent = color
        invalidate()
    }

    private fun animateTo(target: Int) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(animatedIndex, target.toFloat()).apply {
            duration = 240L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                animatedIndex = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun updateDescription() {
        if (options.isEmpty()) return
        contentDescription = "${options[selectedIndex]}, selected. ${options.size} options."
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, context.dpInt(52))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        container.set(0f, 0f, w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        if (options.isEmpty()) return
        val radius = height / 2f
        containerPaint.color = palette.surfaceAlt
        canvas.drawRoundRect(container, radius, radius, containerPaint)

        val segmentWidth = width.toFloat() / options.size
        val inset = context.dp(4f)
        val left = inset + animatedIndex * segmentWidth
        pill.set(left, inset, left + segmentWidth - inset * 2f, height - inset)
        pillPaint.color = palette.surface
        canvas.drawRoundRect(pill, radius - inset, radius - inset, pillPaint)
        strokePaint.strokeWidth = context.dp(1.5f)
        strokePaint.color = withAlpha(accent, 130)
        canvas.drawRoundRect(pill, radius - inset, radius - inset, strokePaint)

        textPaint.textSize = context.sp(13f)
        val baseline = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        options.forEachIndexed { index, label ->
            val centerX = segmentWidth * index + segmentWidth / 2f
            textPaint.color = if (index == selectedIndex) palette.textPrimary else palette.textSecondary
            canvas.drawText(label, centerX, baseline, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                if (options.isNotEmpty()) {
                    val index = (event.x / (width.toFloat() / options.size))
                        .toInt()
                        .coerceIn(0, options.size - 1)
                    if (index != selectedIndex) {
                        setSelected(index, notify = true)
                    }
                    performClick()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                setSelected((selectedIndex - 1).coerceAtLeast(0), notify = true)
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                setSelected((selectedIndex + 1).coerceAtMost(options.size - 1), notify = true)
                return true
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> {
                performClick()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }
}
