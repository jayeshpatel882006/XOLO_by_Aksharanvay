package com.aksharanvay.xolo.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.aksharanvay.xolo.R
import com.aksharanvay.xolo.game.GameState
import com.aksharanvay.xolo.game.GameStatus
import com.aksharanvay.xolo.game.Mark
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * The XOLO board: a floating slab with inset wells and dimensional marks.
 * All depth is drawn in 2D with layered shapes, gradients and highlights.
 */
class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val palette = XoloPalette.from(context)
    private val glyph = GlyphPainter()
    private val boardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cellStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val boardRect = RectF()
    private val shadowRect = RectF()
    private val highlightRect = RectF()
    private val wellShadowRect = RectF()
    private val lipRect = RectF()
    private var boardShader: LinearGradient? = null
    private val cells = Array(9) { RectF() }
    private val placement = FloatArray(9) { 1f }
    private val placementAnimators = arrayOfNulls<ValueAnimator>(9)

    private var state: GameState? = null
    private var interactionEnabled = true
    private var pressedCell = -1
    private var entrance = 1f
    private var entranceAnimator: ValueAnimator? = null
    private var winProgress = 0f
    private var winPulse = 0f
    private var winAnimator: ValueAnimator? = null
    private var pulseAnimator: ValueAnimator? = null

    var onCellTap: ((Int) -> Unit)? = null

    init {
        isClickable = true
        isFocusable = true
        contentDescription = context.getString(R.string.cd_board)
        setWillNotDraw(false)
    }

    fun setState(newState: GameState, animate: Boolean = true) {
        val previous = state
        val previousBoard = previous?.board ?: List(GameState.CELL_COUNT) { null }
        state = newState

        if (previous == null) {
            startEntrance()
        } else if (newState.moveCount == 0 && previous.moveCount > 0) {
            clearPlacementAnimations()
            startEntrance()
        } else {
            for (index in 0 until GameState.CELL_COUNT) {
                if (previousBoard[index] == null && newState.board[index] != null) {
                    startPlacement(index, animate)
                }
            }
        }

        if (newState.winningLine != null) {
            if (newState.winningLine != previous?.winningLine) startWinAnimation()
        } else {
            stopWinAnimation()
        }

        updateDescription()
        invalidate()
    }

    fun setInteractionEnabled(enabled: Boolean) {
        interactionEnabled = enabled
        if (!enabled) pressedCell = -1
        invalidate()
    }

    fun playEntrance() {
        startEntrance()
    }

    private fun startEntrance() {
        entranceAnimator?.cancel()
        entrance = 0f
        entranceAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 720L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                entrance = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun startPlacement(index: Int, animate: Boolean) {
        placementAnimators[index]?.cancel()
        if (!animate || !isAttachedToWindow) {
            placement[index] = 1f
            invalidate()
            return
        }
        placement[index] = 0.15f
        placementAnimators[index] = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 390L
            interpolator = OvershootInterpolator(1.65f)
            addUpdateListener {
                placement[index] = 0.15f + 0.85f * (it.animatedValue as Float)
                invalidate()
            }
            start()
        }
    }

    private fun startWinAnimation() {
        winAnimator?.cancel()
        pulseAnimator?.cancel()
        winProgress = 0f
        winPulse = 0f
        winAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 620L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                winProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1150L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener {
                winPulse = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun stopWinAnimation() {
        winAnimator?.cancel()
        pulseAnimator?.cancel()
        winAnimator = null
        pulseAnimator = null
        winProgress = 0f
        winPulse = 0f
    }

    private fun clearPlacementAnimations() {
        placementAnimators.forEach { it?.cancel() }
        placementAnimators.fill(null)
        placement.fill(1f)
    }

    private fun updateDescription() {
        val current = state ?: return
        val turn = current.currentTurn.name
        val summary = when (current.status) {
            GameStatus.IN_PROGRESS -> "$turn to play. ${current.moveCount} moves played."
            GameStatus.WIN -> "${current.winner?.name} wins."
            GameStatus.DRAW -> "Draw."
        }
        contentDescription = "${context.getString(R.string.cd_board)}. $summary"
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = if (heightMode == MeasureSpec.UNSPECIFIED || height == 0) {
            width
        } else {
            min(width, height)
        }
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = min(w, h).toFloat()
        val left = (w - size) / 2f
        val top = (h - size) / 2f
        boardRect.set(
            left + context.dp(5f),
            top + context.dp(3f),
            left + size - context.dp(5f),
            top + size - context.dp(9f)
        )

        val padding = context.dp(14f)
        val gap = context.dp(9f)
        val inner = min(boardRect.width(), boardRect.height()) - padding * 2f
        val cellSize = (inner - gap * 2f) / 3f
        val startX = boardRect.centerX() - (cellSize * 3f + gap * 2f) / 2f
        val startY = boardRect.centerY() - (cellSize * 3f + gap * 2f) / 2f
        for (row in 0..2) {
            for (column in 0..2) {
                val index = row * 3 + column
                val x = startX + column * (cellSize + gap)
                val y = startY + row * (cellSize + gap)
                cells[index].set(x, y, x + cellSize, y + cellSize)
            }
        }

        boardShader = LinearGradient(
            boardRect.left,
            boardRect.top,
            boardRect.left,
            boardRect.bottom,
            palette.surface,
            palette.surfaceAlt,
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        val current = state ?: return
        val radius = context.dp(28f)

        // Layered slab shadow.
        for (layer in 0..5) {
            shadowPaint.color = withAlpha(palette.shadow, (26 - layer * 3).coerceAtLeast(6))
            shadowRect.set(boardRect)
            shadowRect.offset(0f, context.dp(2f + layer * 1.6f))
            shadowRect.inset(layer * context.dp(0.8f), 0f)
            canvas.drawRoundRect(shadowRect, radius, radius, shadowPaint)
        }

        boardPaint.shader = boardShader
        canvas.drawRoundRect(boardRect, radius, radius, boardPaint)
        boardPaint.shader = null

        cellStroke.strokeWidth = context.dp(1f)
        cellStroke.color = withAlpha(palette.stroke, 22)
        canvas.drawRoundRect(boardRect, radius, radius, cellStroke)

        highlightPaint.strokeWidth = context.dp(1f)
        highlightPaint.color = withAlpha(0xFFFFFFFF.toInt(), 74)
        highlightRect.set(boardRect)
        highlightRect.inset(context.dp(1.5f), context.dp(1.5f))
        canvas.drawRoundRect(highlightRect, radius - context.dp(1.5f), radius - context.dp(1.5f), highlightPaint)

        for (index in 0 until GameState.CELL_COUNT) {
            drawWell(canvas, index)
        }
        for (index in 0 until GameState.CELL_COUNT) {
            val mark = current.board[index] ?: continue
            drawPiece(canvas, index, mark, current)
        }

        val winning = current.winningLine
        if (winning != null && winProgress > 0f) {
            drawWinningLine(canvas, winning)
        }
    }

    private fun drawWell(canvas: Canvas, index: Int) {
        val cell = cells[index]
        if (cell.isEmpty) return
        val stagger = ((entrance - index * 0.035f) / 0.72f).coerceIn(0f, 1f)
        val eased = 1f - (1f - stagger) * (1f - stagger)
        val scale = 0.86f + 0.14f * eased
        val alpha = (eased * 255).toInt()

        canvas.save()
        canvas.scale(scale, scale, cell.centerX(), cell.centerY())

        val wellRadius = context.dp(18f)
        cellPaint.color = withAlpha(palette.boardWellEdge, (90 * alpha / 255))
        wellShadowRect.set(cell)
        wellShadowRect.offset(0f, context.dp(2f))
        canvas.drawRoundRect(wellShadowRect, wellRadius, wellRadius, cellPaint)

        cellPaint.color = if (pressedCell == index) {
            withAlpha(palette.surfaceAlt, alpha)
        } else {
            withAlpha(palette.boardWell, alpha)
        }
        canvas.drawRoundRect(cell, wellRadius, wellRadius, cellPaint)

        cellStroke.strokeWidth = context.dp(1f)
        cellStroke.color = withAlpha(palette.boardWellEdge, (150 * alpha / 255))
        canvas.drawRoundRect(cell, wellRadius, wellRadius, cellStroke)

        // A dark top lip makes the well feel cut into the slab.
        cellStroke.color = withAlpha(palette.shadow, (26 * alpha / 255))
        lipRect.set(cell)
        lipRect.inset(context.dp(2f), context.dp(2f))
        canvas.drawLine(lipRect.left + context.dp(8f), lipRect.top, lipRect.right - context.dp(8f), lipRect.top, cellStroke)
        canvas.restore()
    }

    private fun drawPiece(canvas: Canvas, index: Int, mark: Mark, current: GameState) {
        val cell = cells[index]
        if (cell.isEmpty) return
        val winning = current.winningLine?.contains(index) == true
        val pulse = if (winning) 1f + 0.055f * sin(winPulse * PI.toFloat() * 2f) else 1f
        val scale = placement[index] * pulse
        if (scale <= 0f) return

        val color = if (mark == Mark.X) palette.solar else palette.mint
        canvas.save()
        canvas.translate(cell.centerX(), cell.centerY())
        canvas.scale(scale, scale)
        glyph.drawMark(
            canvas = canvas,
            cx = 0f,
            cy = 0f,
            size = cell.width() * 0.62f,
            mark = mark,
            color = color,
            shadowColor = palette.shadow,
            shadowOffset = context.dp(2.4f),
            glowColor = if (winning) palette.butter else null
        )
        canvas.restore()
    }

    private fun drawWinningLine(canvas: Canvas, line: List<Int>) {
        if (line.size < 2) return
        val start = cells[line.first()]
        val end = cells[line.last()]
        val startX = start.centerX()
        val startY = start.centerY()
        val targetX = end.centerX()
        val targetY = end.centerY()
        val endX = startX + (targetX - startX) * winProgress
        val endY = startY + (targetY - startY) * winProgress
        val width = cells[line.first()].width() * 0.075f

        glowPaint.strokeWidth = width * 2.4f
        glowPaint.color = withAlpha(palette.butter, 38)
        canvas.drawLine(startX, startY, endX, endY, glowPaint)

        linePaint.strokeWidth = width
        linePaint.color = palette.butter
        canvas.drawLine(startX, startY, endX, endY, linePaint)

        linePaint.strokeWidth = width * 0.32f
        linePaint.color = withAlpha(0xFFFFFFFF.toInt(), 120)
        canvas.drawLine(startX, startY, endX, endY, linePaint)

        linePaint.color = palette.butter
        canvas.drawCircle(startX, startY, width * 0.62f, linePaint)
        canvas.drawCircle(endX, endY, width * 0.62f, linePaint)
    }

    private fun cellAt(x: Float, y: Float): Int {
        for (index in cells.indices) {
            if (cells[index].contains(x, y)) return index
        }
        return -1
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val current = state ?: return false
        if (!interactionEnabled || current.isOver) return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val index = cellAt(event.x, event.y)
                if (index >= 0 && current.board[index] == null) {
                    pressedCell = index
                    invalidate()
                    return true
                }
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                val index = cellAt(event.x, event.y)
                val next = if (index >= 0 && current.board[index] == null) index else -1
                if (next != pressedCell) {
                    pressedCell = next
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                val index = cellAt(event.x, event.y)
                val shouldPlay = index >= 0 &&
                    index == pressedCell &&
                    current.board[index] == null
                pressedCell = -1
                invalidate()
                if (shouldPlay) {
                    performClick()
                    onCellTap?.invoke(index)
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                pressedCell = -1
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDetachedFromWindow() {
        entranceAnimator?.cancel()
        winAnimator?.cancel()
        pulseAnimator?.cancel()
        placementAnimators.forEach { it?.cancel() }
        super.onDetachedFromWindow()
    }
}
