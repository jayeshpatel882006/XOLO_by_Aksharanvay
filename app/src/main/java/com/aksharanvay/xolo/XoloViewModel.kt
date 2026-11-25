package com.aksharanvay.xolo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aksharanvay.xolo.ai.AiFactory
import com.aksharanvay.xolo.data.ScoreRepository
import com.aksharanvay.xolo.data.SettingsRepository
import com.aksharanvay.xolo.data.ThemeMode
import com.aksharanvay.xolo.data.XoloSettings
import com.aksharanvay.xolo.game.Difficulty
import com.aksharanvay.xolo.game.GameEngine
import com.aksharanvay.xolo.game.GameMode
import com.aksharanvay.xolo.game.GameStatus
import com.aksharanvay.xolo.game.Mark
import com.aksharanvay.xolo.game.MoveOutcome
import com.aksharanvay.xolo.ui.state.GameEvent
import com.aksharanvay.xolo.ui.state.GameUiState
import com.aksharanvay.xolo.ui.state.XoloUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class XoloViewModel(
    private val settingsRepository: SettingsRepository,
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        XoloUiState(
            settings = settingsRepository.load(),
            scores = scoreRepository.load()
        )
    )
    val uiState: StateFlow<XoloUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    var splashShown: Boolean = false
        private set

    private var aiJob: Job? = null

    fun markSplashShown() {
        splashShown = true
    }

    fun clearGame() {
        cancelAi()
        _uiState.update { it.copy(game = null) }
    }

    fun startFriendGame() {
        cancelAi()
        val settings = _uiState.value.settings
        val game = GameUiState(
            mode = GameMode.FRIEND,
            state = GameEngine.newGame(Mark.X),
            playerMark = Mark.X,
            difficulty = settings.difficulty,
            isComputerThinking = false,
            round = 1,
            scoreRecorded = false
        )
        _uiState.update { it.copy(game = game) }
        _events.tryEmit(GameEvent.Started)
    }

    fun startComputerGame(playerMark: Mark, difficulty: Difficulty) {
        cancelAi()
        val settings = _uiState.value.settings.copy(difficulty = difficulty)
        settingsRepository.save(settings)

        val computerMark = playerMark.other()
        val fresh = GameEngine.newGame(Mark.X)
        val computerStarts = computerMark == Mark.X
        val game = GameUiState(
            mode = GameMode.COMPUTER,
            state = fresh,
            playerMark = playerMark,
            difficulty = difficulty,
            isComputerThinking = computerStarts,
            round = 1,
            scoreRecorded = false
        )
        _uiState.update { it.copy(settings = settings, game = game) }
        _events.tryEmit(GameEvent.Started)
        if (computerStarts) scheduleComputerMove()
    }

    fun playCell(index: Int) {
        val game = _uiState.value.game ?: return
        if (game.state.isOver || game.isComputerThinking) return
        if (game.mode == GameMode.COMPUTER && game.state.currentTurn != game.playerMark) return
        applyMove(index, game)
    }

    fun restartRound() {
        val game = _uiState.value.game ?: return
        cancelAi()
        val fresh = GameEngine.newGame(Mark.X)
        val computerStarts = game.mode == GameMode.COMPUTER && game.computerMark == Mark.X
        val next = game.copy(
            state = fresh,
            isComputerThinking = computerStarts,
            round = game.round + 1,
            scoreRecorded = false
        )
        _uiState.update { it.copy(game = next) }
        if (computerStarts) scheduleComputerMove()
    }

    /**
     * Starts a clean match and clears the score for the active mode.
     * Settings-level score reset clears both modes.
     */
    fun newGame() {
        val game = _uiState.value.game ?: return
        cancelAi()
        val scores = scoreRepository.reset(game.mode)
        val fresh = GameEngine.newGame(Mark.X)
        val computerStarts = game.mode == GameMode.COMPUTER && game.computerMark == Mark.X
        val next = game.copy(
            state = fresh,
            isComputerThinking = computerStarts,
            round = 1,
            scoreRecorded = false
        )
        _uiState.update { it.copy(game = next, scores = scores) }
        if (computerStarts) scheduleComputerMove()
    }

    private fun applyMove(index: Int, game: GameUiState) {
        val outcome = GameEngine.play(game.state, index)
        if (outcome !is MoveOutcome.Accepted) return

        val newState = outcome.state
        val terminal = newState.status != GameStatus.IN_PROGRESS
        var scores = _uiState.value.scores

        if (terminal && !game.scoreRecorded) {
            scores = when (game.mode) {
                GameMode.FRIEND -> scoreRepository.recordFriendResult(newState.winner)
                GameMode.COMPUTER -> scoreRepository.recordComputerResult(
                    when (newState.status) {
                        GameStatus.WIN -> newState.winner == game.playerMark
                        GameStatus.DRAW -> null
                        GameStatus.IN_PROGRESS -> null
                    }
                )
            }
        }

        val computerTurn = game.mode == GameMode.COMPUTER &&
            !terminal &&
            newState.currentTurn == game.computerMark

        val next = game.copy(
            state = newState,
            isComputerThinking = computerTurn,
            scoreRecorded = game.scoreRecorded || terminal
        )
        _uiState.update { it.copy(game = next, scores = scores) }

        when (newState.status) {
            GameStatus.WIN -> _events.tryEmit(GameEvent.Won(newState.winner ?: game.playerMark))
            GameStatus.DRAW -> _events.tryEmit(GameEvent.Drawn)
            GameStatus.IN_PROGRESS -> _events.tryEmit(GameEvent.Placed(newState.board[index] ?: game.playerMark))
        }

        if (computerTurn) scheduleComputerMove()
    }

    private fun scheduleComputerMove() {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            delay(THINK_DELAY_MS)
            val game = _uiState.value.game ?: return@launch
            if (!game.isComputerThinking) return@launch
            val move = AiFactory.create(game.difficulty).chooseMove(game.state) ?: return@launch
            val latest = _uiState.value.game ?: return@launch
            if (!latest.isComputerThinking) return@launch
            applyMove(move, latest)
        }
    }

    private fun cancelAi() {
        aiJob?.cancel()
        aiJob = null
    }

    fun setSoundEnabled(enabled: Boolean) = updateSettings { it.copy(soundEnabled = enabled) }

    fun setVibrationEnabled(enabled: Boolean) = updateSettings { it.copy(vibrationEnabled = enabled) }

    fun setTheme(theme: ThemeMode) = updateSettings { it.copy(theme = theme) }

    fun setDifficulty(difficulty: Difficulty) = updateSettings { it.copy(difficulty = difficulty) }

    fun resetScores() {
        val scores = scoreRepository.resetAll()
        _uiState.update { it.copy(scores = scores) }
    }

    /** Clears every stored score and drops the active board in one step. */
    fun resetEverything() {
        cancelAi()
        val scores = scoreRepository.resetAll()
        _uiState.update { it.copy(game = null, scores = scores) }
    }

    private fun updateSettings(transform: (XoloSettings) -> XoloSettings) {
        val settings = transform(_uiState.value.settings)
        settingsRepository.save(settings)
        _uiState.update { it.copy(settings = settings) }
    }

    override fun onCleared() {
        cancelAi()
        super.onCleared()
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val scoreRepository: ScoreRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(XoloViewModel::class.java)) {
                return XoloViewModel(settingsRepository, scoreRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private companion object {
        const val THINK_DELAY_MS = 620L
    }
}
