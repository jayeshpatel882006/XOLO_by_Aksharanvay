package com.aksharanvay.xolo.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.aksharanvay.xolo.R
import com.aksharanvay.xolo.game.Mark

/**
 * A physical-feeling XOLO surface: layered shadow, soft gradient, top
 * highlight, optional dimensional mark and a springy press response.
 */
class TactileButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val palette = XoloPalette.from(context)
    private val glyphPainter = GlyphPainter()
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val content = RectF()

    private val textColumn: LinearLayout
    private val titleView: TextView
    private val subtitleView: TextView

    private var mark: Mark? = null
    @DrawableRes private var iconRes: Int = 0
    @ColorInt private var accent: Int = palette.solar
    private var small = false
    private var selectedState = false
    private var pressed = false

    init {
        setWillNotDraw(false)
        isClickable = true
        isFocusable = true
        LayoutInflater.from(context).inflate(R.layout.view_tactile_button, this, true)
        textColumn = findViewById(R.id.tactile_text_column)
        titleView = findViewById(R.id.tactile_title)
        subtitleView = findViewById(R.id.tactile_subtitle)
        setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    pressed = true
                    animate()
                        .scaleX(0.975f)
                        .scaleY(0.975f)
                        .translationY(context.dp(2f))
                        .setDuration(90L)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                    invalidate()
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    pressed = false
                    animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .translationY(0f)
                        .setDuration(170L)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                    invalidate()
                }
            }
            false
        }
    }

    fun setMode(
        title: String,
        subtitle: String,
        mark: Mark,
        @ColorInt accent: Int
    ) {
        this.mark = mark
        this.iconRes = 0
        this.accent = accent
        small = false
        titleView.text = title
        subtitleView.text = subtitle
        subtitleView.visibility = VISIBLE
        textColumn.setPadding(
            context.dpInt(22),
            0,
            context.dpInt(92),
            0
        )
        updateContentDescription(title, subtitle)
        invalidate()
    }

    fun setAction(
        title: String,
        @ColorInt accent: Int,
        @DrawableRes icon: Int = 0
    ) {
        mark = null
        iconRes = icon
        this.accent = accent
        small = true
        titleView.text = title
        subtitleView.visibility = GONE
        textColumn.setPadding(
            context.dpInt(20),
            0,
            context.dpInt(if (icon != 0) 58 else 20),
            0
        )
        updateContentDescription(title, null)
        invalidate()
    }

    fun setSelectedState(selected: Boolean) {
        if (selectedState == selected) return
        selectedState = selected
        invalidate()
    }

    fun setAccent(@ColorInt color: Int) {
        accent = color
        invalidate()
    }

    private fun updateContentDescription(title: String, subtitle: String?) {
        contentDescription = if (subtitle.isNullOrBlank()) title else "$title. $subtitle"
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val height = if (heightMode == MeasureSpec.UNSPECIFIED) {
            context.dpInt(if (small) 58 else 108)
        } else {
            MeasureSpec.getSize(heightMeasureSpec)
        }
        // Measure children against a fixed box so the label column lays out,
        // then keep the exact size this view was asked to occupy.
        val exactWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        val exactHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(exactWidth, exactHeight)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val inset = context.dp(2f)
        content.set(inset, inset, w - inset, h - context.dp(6f))
    }

    override fun onDraw(canvas: Canvas) {
        if (content.isEmpty) return
        val radius = if (small) content.height() / 2f else context.dp(26f)

        // Soft layered shadow, kept inside the view bounds.
        shadowPaint.color = withAlpha(palette.shadow, if (pressed) 18 else 28)
        for (layer in 0..3) {
            val offset = context.dp(2f + layer * 1.4f)
            shadowPaint.color = withAlpha(palette.shadow, if (pressed) 16 else 30 - layer * 5)
            val shadowRect = RectF(content)
            shadowRect.offset(0f, offset)
            shadowRect.inset(layer * context.dp(0.6f), 0f)
            canvas.drawRoundRect(shadowRect, radius, radius, shadowPaint)
        }

        val top = if (selectedState) mix(palette.surface, accent, 0.14f) else palette.surface
        val bottom = if (selectedState) mix(palette.surfaceAlt, accent, 0.16f) else palette.surfaceAlt
        fillPaint.shader = LinearGradient(
            0f, content.top, 0f, content.bottom,
            top, bottom, Shader.TileMode.CLAMP
        )
        fillPaint.alpha = if (pressed) 238 else 255
        canvas.drawRoundRect(content, radius, radius, fillPaint)
        fillPaint.shader = null

        strokePaint.strokeWidth = context.dp(if (selectedState) 2f else 1f)
        strokePaint.color = if (selectedState) withAlpha(accent, 190) else withAlpha(palette.stroke, 22)
        canvas.drawRoundRect(content, radius, radius, strokePaint)

        highlightPaint.strokeWidth = context.dp(1f)
        highlightPaint.color = withAlpha(0xFFFFFFFF.toInt(), if (selectedState) 96 else 72)
        val highlight = RectF(content)
        highlight.inset(context.dp(1f), context.dp(1f))
        canvas.drawRoundRect(highlight, radius - context.dp(1f), radius - context.dp(1f), highlightPaint)

        // Accent bar for large cards.
        if (!small) {
            accentPaint.color = accent
            val bar = RectF(
                content.left + context.dp(10f),
                content.centerY() - context.dp(15f),
                content.left + context.dp(14f),
                content.centerY() + context.dp(15f)
            )
            canvas.drawRoundRect(bar, context.dp(2f), context.dp(2f), accentPaint)
        }

        // Icon medallion.
        val hasIcon = mark != null || iconRes != 0
        if (hasIcon) {
            val iconRadius = context.dp(if (small) 16f else 22f)
            val cx = content.right - context.dp(if (small) 30f else 42f)
            val cy = content.centerY()
            iconCirclePaint.color = withAlpha(accent, 34)
            canvas.drawCircle(cx, cy, iconRadius, iconCirclePaint)
            iconCirclePaint.color = withAlpha(accent, 80)
            iconCirclePaint.style = Paint.Style.STROKE
            iconCirclePaint.strokeWidth = context.dp(1f)
            canvas.drawCircle(cx, cy, iconRadius, iconCirclePaint)
            iconCirclePaint.style = Paint.Style.FILL

            val markValue = mark
            if (markValue != null) {
                glyphPainter.drawMark(
                    canvas = canvas,
                    cx = cx,
                    cy = cy,
                    size = context.dp(if (small) 22f else 31f),
                    mark = markValue,
                    color = accent,
                    shadowColor = palette.shadow,
                    shadowOffset = context.dp(1.5f)
                )
            } else if (iconRes != 0) {
                val drawable: Drawable? = context.getDrawable(iconRes)
                drawable?.mutate()?.setTint(accent)
                val size = context.dpInt(if (small) 17 else 22)
                drawable?.setBounds(
                    (cx - size / 2f).toInt(),
                    (cy - size / 2f).toInt(),
                    (cx + size / 2f).toInt(),
                    (cy + size / 2f).toInt()
                )
                drawable?.draw(canvas)
            }
        }
    }
}
