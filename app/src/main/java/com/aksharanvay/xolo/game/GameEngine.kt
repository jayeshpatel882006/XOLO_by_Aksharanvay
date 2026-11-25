package com.aksharanvay.xolo.game

/**
 * Pure Kotlin rules for XOLO. No Android types, no I/O, no randomness.
 *
 * The engine is intentionally small so it can be exhaustively unit tested and
 * reused by the computer opponent.
 */
object GameEngine {

    val WIN_LINES: List<List<Int>> = listOf(
        listOf(0, 1, 2),
        listOf(3, 4, 5),
        listOf(6, 7, 8),
        listOf(0, 3, 6),
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        listOf(0, 4, 8),
        listOf(2, 4, 6)
    )

    fun newGame(startingMark: Mark = Mark.X): GameState = GameState(
        board = List(GameState.CELL_COUNT) { null },
        currentTurn = startingMark,
        status = GameStatus.IN_PROGRESS,
        winner = null,
        winningLine = null,
        moveCount = 0,
        lastMove = null
    )

    fun play(state: GameState, index: Int): MoveOutcome {
        if (state.status != GameStatus.IN_PROGRESS) {
            return MoveOutcome.Rejected(MoveError.GAME_OVER)
        }
        if (index !in 0 until GameState.CELL_COUNT) {
            return MoveOutcome.Rejected(MoveError.INVALID_INDEX)
        }
        if (state.board[index] != null) {
            return MoveOutcome.Rejected(MoveError.CELL_OCCUPIED)
        }

        val nextBoard = state.board.toMutableList().apply {
            this[index] = state.currentTurn
        }
        val win = findWin(nextBoard)
        val nextMoveCount = state.moveCount + 1

        val nextStatus = when {
            win != null -> GameStatus.WIN
            nextMoveCount == GameState.CELL_COUNT -> GameStatus.DRAW
            else -> GameStatus.IN_PROGRESS
        }

        val next = state.copy(
            board = nextBoard,
            currentTurn = if (nextStatus == GameStatus.IN_PROGRESS) {
                state.currentTurn.other()
            } else {
                state.currentTurn
            },
            status = nextStatus,
            winner = win?.first,
            winningLine = win?.second,
            moveCount = nextMoveCount,
            lastMove = index
        )
        return MoveOutcome.Accepted(next)
    }

    fun availableMoves(state: GameState): List<Int> =
        if (state.isOver) emptyList()
        else state.board.indices.filter { state.board[it] == null }

    /**
     * Returns the winning mark and line, or null when nobody has won yet.
     */
    fun findWin(board: List<Mark?>): Pair<Mark, List<Int>>? {
        for (line in WIN_LINES) {
            val first = board[line[0]]
            if (first != null && first == board[line[1]] && first == board[line[2]]) {
                return first to line
            }
        }
        return null
    }
}
