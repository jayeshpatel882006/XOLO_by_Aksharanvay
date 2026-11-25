package com.aksharanvay.xolo.ai

import com.aksharanvay.xolo.game.GameEngine
import com.aksharanvay.xolo.game.GameState
import kotlin.random.Random

/**
 * Picks any legal square. Deliberately unpredictable and easy to beat.
 */
class EasyAi(private val random: Random = Random.Default) : XoloAi {
    override fun chooseMove(state: GameState): Int? {
        val moves = GameEngine.availableMoves(state)
        if (moves.isEmpty()) return null
        return moves[random.nextInt(moves.size)]
    }
}
