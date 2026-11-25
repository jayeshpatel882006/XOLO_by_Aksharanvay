package com.aksharanvay.xolo.ai

import com.aksharanvay.xolo.game.GameEngine
import com.aksharanvay.xolo.game.GameState
import com.aksharanvay.xolo.game.Mark
import com.aksharanvay.xolo.game.MoveOutcome
import kotlin.random.Random

/**
 * A friendly club player: take a win, block a loss, then prefer the centre,
 * an opposite corner and finally a random legal square.
 */
class MediumAi(private val random: Random = Random.Default) : XoloAi {

    override fun chooseMove(state: GameState): Int? {
        val moves = GameEngine.availableMoves(state)
        if (moves.isEmpty()) return null

        val me = state.currentTurn
        val opponent = me.other()

        // 1. Win now.
        moves.firstOrNull { wouldWin(state, it, me) }?.let { return it }

        // 2. Stop the opponent winning next move.
        moves.firstOrNull { wouldWin(state, it, opponent) }?.let { return it }

        // 3. Own the centre.
        if (CENTER in moves) return CENTER

        // 4. Take the corner opposite an opponent's corner.
        val opposite = mapOf(0 to 8, 2 to 6, 6 to 2, 8 to 0)
        val opponentCorners = CORNERS.filter { state.board[it] == opponent }.shuffled(random)
        for (corner in opponentCorners) {
            val target = opposite.getValue(corner)
            if (target in moves) return target
        }

        // 5. Any corner, then any side.
        val corners = CORNERS.filter { it in moves }
        if (corners.isNotEmpty()) return corners[random.nextInt(corners.size)]

        return moves[random.nextInt(moves.size)]
    }

    private fun wouldWin(state: GameState, index: Int, mark: Mark): Boolean {
        if (state.board[index] != null) return false
        val probe = state.copy(
            currentTurn = mark,
            status = com.aksharanvay.xolo.game.GameStatus.IN_PROGRESS,
            winner = null,
            winningLine = null
        )
        val result = GameEngine.play(probe, index)
        return result is MoveOutcome.Accepted && result.state.winner == mark
    }

    private companion object {
        const val CENTER = 4
        val CORNERS = listOf(0, 2, 6, 8)
    }
}
