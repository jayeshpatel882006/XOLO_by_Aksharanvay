package com.aksharanvay.xolo.game

enum class MoveError {
    INVALID_INDEX,
    CELL_OCCUPIED,
    GAME_OVER
}

sealed interface MoveOutcome {
    data class Accepted(val state: GameState) : MoveOutcome
    data class Rejected(val reason: MoveError) : MoveOutcome
}
