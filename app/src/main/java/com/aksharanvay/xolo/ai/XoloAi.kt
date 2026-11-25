package com.aksharanvay.xolo.ai

import com.aksharanvay.xolo.game.GameState

/**
 * A computer opponent. Implementations are pure and receive the current state.
 * Returning null means there is no legal move (the round is over).
 */
interface XoloAi {
    fun chooseMove(state: GameState): Int?
}
