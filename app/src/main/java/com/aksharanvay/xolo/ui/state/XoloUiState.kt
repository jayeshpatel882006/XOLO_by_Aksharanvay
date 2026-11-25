package com.aksharanvay.xolo.ui.state

import com.aksharanvay.xolo.data.XoloScores
import com.aksharanvay.xolo.data.XoloSettings
import com.aksharanvay.xolo.game.Difficulty
import com.aksharanvay.xolo.game.GameMode
import com.aksharanvay.xolo.game.GameState
import com.aksharanvay.xolo.game.Mark

data class XoloUiState(
    val settings: XoloSettings,
    val scores: XoloScores,
    val game: GameUiState? = null
)

data class GameUiState(
    val mode: GameMode,
    val state: GameState,
    val playerMark: Mark,
    val difficulty: Difficulty,
    val isComputerThinking: Boolean,
    val round: Int,
    val scoreRecorded: Boolean
) {
    val computerMark: Mark get() = playerMark.other()
    val isHumanTurn: Boolean
        get() = mode == GameMode.FRIEND || state.currentTurn == playerMark
}

sealed interface GameEvent {
    data class Placed(val mark: Mark) : GameEvent
    data class Won(val winner: Mark) : GameEvent
    data object Drawn : GameEvent
    data object Started : GameEvent
}
