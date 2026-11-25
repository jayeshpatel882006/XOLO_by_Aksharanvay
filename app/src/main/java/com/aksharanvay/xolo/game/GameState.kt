package com.aksharanvay.xolo.game

/**
 * Immutable snapshot of a tic-tac-toe round.
 *
 * [board] always has exactly nine entries; null means an open square.
 */
data class GameState(
    val board: List<Mark?>,
    val currentTurn: Mark,
    val status: GameStatus,
    val winner: Mark?,
    val winningLine: List<Int>?,
    val moveCount: Int,
    val lastMove: Int?
) {
    val isOver: Boolean get() = status != GameStatus.IN_PROGRESS

    companion object {
        const val CELL_COUNT = 9
    }
}
