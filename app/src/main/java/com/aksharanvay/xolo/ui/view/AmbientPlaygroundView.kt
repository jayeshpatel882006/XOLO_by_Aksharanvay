package com.aksharanvay.xolo.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import com.aksharanvay.xolo.R
import com.aksharanvay.xolo.game.Mark
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * The living XOLO backdrop: a warm field, slow colour weather and a handful of
 * drifting dimensional marks. Touching a mark makes it flinch and throw a tiny
 * burst of confetti.
 */
class AmbientPlaygroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val palette = XoloPalette.from(context)
    private val painter = GlyphPainter()
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blobPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = context.dp(1f)
    }

    private var backgroundShader: Shader? = null
    private var solarShader: Shader? = null
    private var mintShader: Shader? = null
    private var lilacShader: Shader? = null

    private var time = 0f
    private var animator: ValueAnimator? = null
    private var intensity = 1f

    private var bumpIndex = -1
    private var bumpStart = 0L
    private val bursts = mutableListOf<Burst>()

    var onPlayfulTap: (() -> Unit)? = null

    private val shapes = listOf(
        FloatShape(Mark.X, 0.12f, 0.16f, 52f, 0.2f, 0.45f, 0.20f, palette.solar),
        FloatShape(Mark.O, 0.86f, 0.12f, 42f, 1.4f, 0.38f, 0.24f, palette.mint),
        FloatShape(Mark.X, 0.90f, 0.46f, 34f, 2.2f, 0.55f, 0.18f, palette.lilac),
        FloatShape(Mark.O, 0.09f, 0.52f, 30f, 3.1f, 0.50f, 0.20f, palette.butter),
        FloatShape(Mark.X, 0.18f, 0.82f, 38f, 4.0f, 0.42f, 0.22f, palette.mint),
        FloatShape(Mark.O, 0.82f, 0.84f, 48f, 5.2f, 0.34f, 0.26f, palette.solar),
        FloatShape(Mark.X, 0.48f, 0.07f, 24f, 2.8f, 0.62f, 0.16f, palette.butter),
        FloatShape(Mark.O, 0.55f, 0.94f, 26f, 1.9f, 0.48f, 0.18f, palette.lilac)
    )

    init {
        isClickable = true
        isFocusable = true
        contentDescription = context.getString(R.string.cd_ambient)
        setWillNotDraw(false)
    }

    fun setIntensity(value: Float) {
        intensity = value.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        backgroundShader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            palette.paper,
            palette.paperDeep,
            Shader.TileMode.CLAMP
        )
        val radius = maxOf(w, h) * 0.62f
        solarShader = RadialGradient(
            0f, 0f, radius,
            intArrayOf(withAlpha(palette.solar, 42), withAlpha(palette.solar, 0)),
            null,
            Shader.TileMode.CLAMP
        )
        mintShader = RadialGradient(
            0f, 0f, radius,
            intArrayOf(withAlpha(palette.mint, 38), withAlpha(palette.mint, 0)),
            null,
            Shader.TileMode.CLAMP
        )
        lilacShader = RadialGradient(
            0f, 0f, radius,
            intArrayOf(withAlpha(palette.lilac, 34), withAlpha(palette.lilac, 0)),
            null,
            Shader.TileMode.CLAMP
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    private fun start() {
        if (animator != null) return
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 18000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                time = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        backgroundPaint.shader = backgroundShader
        canvas.drawRect(0f, 0f, w, h, backgroundPaint)
        backgroundPaint.shader = null

        drawBlob(canvas, w * 0.16f, h * 0.20f, solarShader, 0.6f)
        drawBlob(canvas, w * 0.86f, h * 0.36f, mintShader, 1.4f)
        drawBlob(canvas, w * 0.28f, h * 0.86f, lilacShader, 2.6f)

        val angle = time * PI.toFloat() * 2f
        val now = SystemClock.uptimeMillis()

        shapes.forEachIndexed { index, shape ->
            val driftX = sin(angle * shape.speed + shape.phase) * w * 0.026f
            val driftY = cos(angle * shape.speed * 0.8f + shape.phase) * h * 0.018f
            val cx = w * shape.x + driftX
            val cy = h * shape.y + driftY

            var scale = 1f
            if (index == bumpIndex) {
                val t = (now - bumpStart) / 420f
                if (t in 0f..1f) {
                    scale = 1f + 0.22f * sin(t * PI.toFloat())
                } else {
                    bumpIndex = -1
                }
            }

            canvas.save()
            canvas.translate(cx, cy)
            canvas.rotate(sin(angle * shape.speed + shape.phase) * 7f)
            canvas.scale(scale, scale)
            painter.drawSimple(
                canvas = canvas,
                cx = 0f,
                cy = 0f,
                size = context.dp(shape.sizeDp),
                mark = shape.mark,
                color = shape.color,
                alpha = (shape.alpha * intensity * 255).toInt(),
                strokeScale = 0.9f
            )
            canvas.restore()
        }

        // Quiet geometric punctuation.
        dotPaint.color = withAlpha(palette.textSecondary, (18 * intensity).toInt())
        for (i in 0..11) {
            val x = ((i * 97) % 100) / 100f * w
            val y = ((i * 61 + 17) % 100) / 100f * h
            canvas.drawCircle(x, y, context.dp(1.3f), dotPaint)
        }

        ringPaint.color = withAlpha(palette.stroke, (14 * intensity).toInt())
        canvas.drawCircle(w * 0.74f, h * 0.70f, context.dp(44f), ringPaint)
        canvas.drawCircle(w * 0.24f, h * 0.36f, context.dp(24f), ringPaint)

        drawBursts(canvas, now)
    }

    private fun drawBlob(canvas: Canvas, cx: Float, cy: Float, shader: Shader?, phase: Float) {
        if (shader == null) return
        val drift = sin(time * PI.toFloat() * 2f + phase) * context.dp(18f)
        canvas.save()
        canvas.translate(cx + drift, cy + drift * 0.6f)
        blobPaint.shader = shader
        blobPaint.alpha = (110 * intensity).toInt()
        canvas.drawCircle(0f, 0f, maxOf(width, height) * 0.62f, blobPaint)
        canvas.restore()
        blobPaint.shader = null
    }

    private fun drawBursts(canvas: Canvas, now: Long) {
        if (bursts.isEmpty()) return
        val iterator = bursts.iterator()
        while (iterator.hasNext()) {
            val burst = iterator.next()
            val t = (now - burst.start) / 620f
            if (t >= 1f) {
                iterator.remove()
                continue
            }
            for (i in 0 until burst.count) {
                val a = (PI * 2 / burst.count * i).toFloat() + burst.phase
                val distance = context.dp(12f) + context.dp(52f) * t
                val x = burst.x + cos(a) * distance
                val y = burst.y + sin(a) * distance + context.dp(18f) * t * t
                dotPaint.color = withAlpha(burst.color, ((1f - t) * 190).toInt())
                canvas.drawCircle(x, y, context.dp(2.4f) * (1f - t * 0.4f), dotPaint)
            }
        }
        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            val nearest = shapes.indices.minByOrNull { index ->
                val shape = shapes[index]
                val cx = width * shape.x
                val cy = height * shape.y
                hypot(event.x - cx, event.y - cy)
            }
            if (nearest != null) {
                val shape = shapes[nearest]
                val cx = width * shape.x
                val cy = height * shape.y
                if (hypot(event.x - cx, event.y - cy) < context.dp(72f)) {
                    bumpIndex = nearest
                    bumpStart = SystemClock.uptimeMillis()
                    bursts += Burst(
                        x = cx,
                        y = cy,
                        start = bumpStart,
                        color = shape.color,
                        count = 10,
                        phase = (nearest * 0.7f)
                    )
                    if (bursts.size > 4) bursts.removeAt(0)
                    onPlayfulTap?.invoke()
                    performClick()
                    invalidate()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        bursts.clear()
        super.onDetachedFromWindow()
    }

    private data class FloatShape(
        val mark: Mark,
        val x: Float,
        val y: Float,
        val sizeDp: Float,
        val phase: Float,
        val speed: Float,
        val alpha: Float,
        val color: Int
    )

    private data class Burst(
        val x: Float,
        val y: Float,
        val start: Long,
        val color: Int,
        val count: Int,
        val phase: Float
    )
}
