package com.aksharanvay.xolo.ui.view

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.annotation.ColorInt
import com.aksharanvay.xolo.game.Mark

/**
 * Draws the dimensional X and O marks used throughout XOLO.
 *
 * The depth is deliberately lightweight: a soft offset shadow, a diagonal
 * gradient body and a single specular highlight. No bitmaps, no 3D engine.
 */
class GlyphPainter {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val arcRect = RectF()

    fun drawMark(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        size: Float,
        mark: Mark,
        @ColorInt color: Int,
        @ColorInt shadowColor: Int,
        alpha: Int = 255,
        shadowOffset: Float = size * 0.045f,
        glowColor: Int? = null
    ) {
        if (size <= 0f) return

        if (glowColor != null) {
            fill.shader = null
            fill.color = withAlpha(glowColor, (70 * alpha / 255))
            canvas.drawCircle(cx, cy, size * 0.56f, fill)
            fill.color = withAlpha(glowColor, (38 * alpha / 255))
            canvas.drawCircle(cx, cy, size * 0.68f, fill)
        }

        val half = size * 0.31f
        val stroke = size * 0.165f
        shadowPaint.color = withAlpha(shadowColor, (70 * alpha / 255))
        shadowPaint.strokeWidth = stroke
        shadowPaint.shader = null

        when (mark) {
            Mark.X -> {
                canvas.drawLine(
                    cx - half, cy - half + shadowOffset,
                    cx + half, cy + half + shadowOffset,
                    shadowPaint
                )
                canvas.drawLine(
                    cx + half, cy - half + shadowOffset,
                    cx - half, cy + half + shadowOffset,
                    shadowPaint
                )
            }
            Mark.O -> {
                val radius = size * 0.315f
                canvas.drawCircle(cx, cy + shadowOffset, radius, shadowPaint)
            }
        }

        paint.shader = LinearGradient(
            cx - half,
            cy - half,
            cx + half,
            cy + half,
            lighten(color, 0.34f),
            darken(color, 0.14f),
            Shader.TileMode.CLAMP
        )
        paint.strokeWidth = stroke
        paint.alpha = alpha

        when (mark) {
            Mark.X -> {
                canvas.drawLine(cx - half, cy - half, cx + half, cy + half, paint)
                canvas.drawLine(cx + half, cy - half, cx - half, cy + half, paint)
            }
            Mark.O -> {
                val radius = size * 0.315f
                canvas.drawCircle(cx, cy, radius, paint)
            }
        }

        // Specular highlight: one soft, off-centre pass.
        paint.shader = null
        paint.color = withAlpha(0xFFFFFFFF.toInt(), (88 * alpha / 255))
        paint.strokeWidth = stroke * 0.26f
        when (mark) {
            Mark.X -> {
                canvas.drawLine(
                    cx - half * 0.82f,
                    cy - half * 0.94f,
                    cx - half * 0.18f,
                    cy - half * 0.30f,
                    paint
                )
            }
            Mark.O -> {
                val radius = size * 0.315f
                arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)
                canvas.drawArc(arcRect, 205f, 72f, false, paint)
            }
        }
        paint.alpha = 255
    }

    fun drawSimple(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        size: Float,
        mark: Mark,
        @ColorInt color: Int,
        alpha: Int = 255,
        strokeScale: Float = 1f
    ) {
        paint.shader = null
        paint.color = withAlpha(color, alpha)
        paint.strokeWidth = size * 0.16f * strokeScale
        val half = size * 0.31f
        when (mark) {
            Mark.X -> {
                canvas.drawLine(cx - half, cy - half, cx + half, cy + half, paint)
                canvas.drawLine(cx + half, cy - half, cx - half, cy + half, paint)
            }
            Mark.O -> {
                canvas.drawCircle(cx, cy, size * 0.315f, paint)
            }
        }
    }
}
