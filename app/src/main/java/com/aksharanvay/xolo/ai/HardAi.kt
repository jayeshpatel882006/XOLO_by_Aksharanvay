package com.aksharanvay.xolo.ai

import com.aksharanvay.xolo.game.GameEngine
import com.aksharanvay.xolo.game.GameState
import com.aksharanvay.xolo.game.GameStatus
import com.aksharanvay.xolo.game.Mark
import com.aksharanvay.xolo.game.MoveOutcome
import kotlin.math.max
import kotlin.math.min

/**
 * Optimal play using minimax with alpha-beta pruning.
 *
 * Equal-scoring moves are ordered centre, corners, sides so the opponent feels
 * purposeful rather than arbitrary. Depth is used in the score so the AI takes
 * the quickest win and the longest defence.
 */
class HardAi : XoloAi {

    override fun chooseMove(state: GameState): Int? {
        val moves = ordered(GameEngine.availableMoves(state))
        if (moves.isEmpty()) return null

        val me = state.currentTurn
        var bestScore = Int.MIN_VALUE
        var bestMove = moves.first()
        var alpha = Int.MIN_VALUE

        for (move in moves) {
            val next = (GameEngine.play(state, move) as MoveOutcome.Accepted).state
            val score = minimax(next, me, depth = 1, alpha = alpha, beta = Int.MAX_VALUE)
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
            alpha = max(alpha, bestScore)
        }
        return bestMove
    }

    private fun minimax(
        state: GameState,
        aiMark: Mark,
        depth: Int,
        alpha: Int,
        beta: Int
    ): Int {
        when (state.status) {
            GameStatus.WIN -> {
                return if (state.winner == aiMark) 10 - depth else depth - 10
            }
            GameStatus.DRAW -> return 0
            GameStatus.IN_PROGRESS -> Unit
        }

        val moves = ordered(GameEngine.availableMoves(state))
        if (state.currentTurn == aiMark) {
            var best = Int.MIN_VALUE
            var a = alpha
            for (move in moves) {
                val next = (GameEngine.play(state, move) as MoveOutcome.Accepted).state
                best = max(best, minimax(next, aiMark, depth + 1, a, beta))
                a = max(a, best)
                if (beta <= a) break
            }
            return best
        }

        var best = Int.MAX_VALUE
        var b = beta
        for (move in moves) {
            val next = (GameEngine.play(state, move) as MoveOutcome.Accepted).state
            best = min(best, minimax(next, aiMark, depth + 1, alpha, b))
            b = min(b, best)
            if (b <= alpha) break
        }
        return best
    }

    private fun ordered(moves: List<Int>): List<Int> = moves.sortedBy { move ->
        when (move) {
            4 -> 0
            0, 2, 6, 8 -> 1
            else -> 2
        }
    }
}
