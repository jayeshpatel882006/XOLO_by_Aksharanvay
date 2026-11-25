package com.aksharanvay.xolo.ui.screen

import android.content.Context
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.aksharanvay.xolo.R
import com.aksharanvay.xolo.XoloViewModel
import com.aksharanvay.xolo.game.Difficulty
import com.aksharanvay.xolo.game.GameMode
import com.aksharanvay.xolo.game.GameStatus
import com.aksharanvay.xolo.game.Mark
import com.aksharanvay.xolo.ui.XoloNavigator
import com.aksharanvay.xolo.ui.state.GameUiState
import com.aksharanvay.xolo.ui.state.XoloUiState
import com.aksharanvay.xolo.ui.view.AmbientPlaygroundView
import com.aksharanvay.xolo.ui.view.BoardView
import com.aksharanvay.xolo.ui.view.MarkGlyphView
import com.aksharanvay.xolo.ui.view.ScoreStripView
import com.aksharanvay.xolo.ui.view.SegmentedControl
import com.aksharanvay.xolo.ui.view.TactileButton
import com.aksharanvay.xolo.ui.view.ThinkingIndicator
import com.aksharanvay.xolo.ui.view.XoloDialog
import com.aksharanvay.xolo.ui.view.XoloPalette

class GameScreen(
    context: Context,
    private val mode: GameMode,
    private val viewModel: XoloViewModel,
    private val navigator: XoloNavigator
) : XoloScreen(context, R.layout.screen_game) {

    private val palette = XoloPalette.from(context)
    private val ambient = findViewById<AmbientPlaygroundView>(R.id.game_ambient)
    private val modeChip = findViewById<TextView>(R.id.game_mode)
    private val scores = findViewById<ScoreStripView>(R.id.game_scores)
    private val board = findViewById<BoardView>(R.id.game_board)
    private val turnMark = findViewById<MarkGlyphView>(R.id.turn_mark)
    private val turnTitle = findViewById<TextView>(R.id.turn_title)
    private val turnSubtitle = findViewById<TextView>(R.id.turn_subtitle)
    private val thinking = findViewById<ThinkingIndicator>(R.id.turn_thinking)
    private val status = findViewById<TextView>(R.id.game_status)
    private val restart = findViewById<TactileButton>(R.id.game_restart)
    private val newGame = findViewById<TactileButton>(R.id.game_new)

    private val setupOverlay = findViewById<FrameLayout>(R.id.setup_overlay)
    private val setupCard = findViewById<LinearLayout>(R.id.setup_card)
    private val setupX = findViewById<TactileButton>(R.id.setup_x)
    private val setupO = findViewById<TactileButton>(R.id.setup_o)
    private val setupDifficulty = findViewById<SegmentedControl>(R.id.setup_difficulty)
    private val setupStart = findViewById<TactileButton>(R.id.setup_start)
    private val setupClose = findViewById<ImageButton>(R.id.setup_close)
    private val setupCancel = findViewById<TextView>(R.id.setup_cancel)

    private var selectedMark = Mark.X
    private var selectedDifficulty = Difficulty.HARD
    private var lastTurn: Mark? = null
    private var lastAnnouncedMoveCount = -1
    private var setupVisible = false

    init {
        ambient.setIntensity(0.42f)
        restart.setAction(context.getString(R.string.restart), palette.lilac, R.drawable.ic_restart)
        newGame.setAction(context.getString(R.string.new_game), palette.solar, R.drawable.ic_play)
        setupX.setMode(
            context.getString(R.string.choose_x),
            context.getString(R.string.choose_x_sub),
            Mark.X,
            palette.solar
        )
        setupO.setMode(
            context.getString(R.string.choose_o),
            context.getString(R.string.choose_o_sub),
            Mark.O,
            palette.mint
        )
        setupStart.setAction(context.getString(R.string.setup_start), palette.solar, R.drawable.ic_play)
        setupDifficulty.setAccent(palette.mint)

        findViewById<ImageButton>(R.id.game_back).apply {
            setOnClickListener {
                navigator.tapFeedback()
                navigator.goHome()
            }
        }

        restart.setOnClickListener {
            navigator.tapFeedback()
            viewModel.restartRound()
        }
        newGame.setOnClickListener {
            navigator.tapFeedback()
            XoloDialog(context).show(
                title = context.getString(R.string.new_game_dialog_title),
                message = context.getString(R.string.new_game_dialog_body),
                confirmLabel = context.getString(R.string.new_game_confirm),
                cancelLabel = context.getString(R.string.new_game_cancel)
            ) {
                viewModel.newGame()
            }
        }

        board.onCellTap = { index -> viewModel.playCell(index) }

        setupX.setOnClickListener {
            navigator.tapFeedback()
            selectedMark = Mark.X
            updateSetupSelection()
        }
        setupO.setOnClickListener {
            navigator.tapFeedback()
            selectedMark = Mark.O
            updateSetupSelection()
        }
        setupDifficulty.onSelected = { index ->
            selectedDifficulty = Difficulty.values()[index]
            viewModel.setDifficulty(selectedDifficulty)
            navigator.tapFeedback()
        }
        setupStart.setOnClickListener {
            navigator.tapFeedback()
            viewModel.startComputerGame(selectedMark, selectedDifficulty)
            hideSetup()
        }
        val closeSetup = View.OnClickListener {
            navigator.tapFeedback()
            hideSetup()
            navigator.goHome()
        }
        setupClose.setOnClickListener(closeSetup)
        setupCancel.setOnClickListener(closeSetup)
    }

    override fun onShown() {
        val game = viewModel.uiState.value.game
        if (mode == GameMode.COMPUTER) {
            if (game == null || game.mode != GameMode.COMPUTER) showSetup()
        } else if (game == null || game.mode != GameMode.FRIEND) {
            viewModel.startFriendGame()
        }
        board.post { board.playEntrance() }
    }

    override fun render(state: XoloUiState) {
        val game = state.game ?: return
        if (game.mode != mode) return
        if (game.state.moveCount == 0) lastAnnouncedMoveCount = -1

        ambient.setIntensity(0.42f)
        modeChip.text = when (game.mode) {
            GameMode.FRIEND -> context.getString(R.string.mode_friend)
            GameMode.COMPUTER -> "${context.getString(R.string.mode_computer)} / ${difficultyLabel(game.difficulty)}"
        }

        val modeScores = state.scores.forMode(game.mode)
        if (game.mode == GameMode.FRIEND) {
            scores.setScores(
                modeScores.left,
                modeScores.draws,
                modeScores.right,
                context.getString(R.string.score_label_x),
                context.getString(R.string.score_label_o),
                context.getString(R.string.score_label_draws)
            )
        } else {
            scores.setScores(
                modeScores.left,
                modeScores.draws,
                modeScores.right,
                context.getString(R.string.score_label_you),
                context.getString(R.string.score_label_cpu),
                context.getString(R.string.score_label_draws)
            )
        }

        board.setState(game.state)
        board.setInteractionEnabled(
            game.isHumanTurn && !game.isComputerThinking && !game.state.isOver
        )

        val turn = game.state.currentTurn
        turnMark.setMark(turn, animate = lastTurn != null && lastTurn != turn)
        lastTurn = turn
        thinking.setActive(game.isComputerThinking)
        thinking.visibility = if (game.isComputerThinking) View.VISIBLE else View.GONE

        when {
            game.state.isOver -> {
                turnTitle.text = context.getString(R.string.game_over)
                turnSubtitle.text = context.getString(R.string.turn_over_sub)
            }
            game.isComputerThinking -> {
                turnTitle.text = context.getString(R.string.turn_computer)
                turnSubtitle.text = context.getString(R.string.turn_computer_sub)
            }
            game.mode == GameMode.COMPUTER -> {
                turnTitle.text = context.getString(R.string.turn_your)
                turnSubtitle.text = context.getString(R.string.turn_your_sub)
            }
            else -> {
                turnTitle.text = if (turn == Mark.X) {
                    context.getString(R.string.turn_x)
                } else {
                    context.getString(R.string.turn_o)
                }
                turnSubtitle.text = context.getString(R.string.turn_friend_sub)
            }
        }

        status.text = when (game.state.status) {
            GameStatus.WIN -> when (game.mode) {
                GameMode.FRIEND -> if (game.state.winner == Mark.X) {
                    context.getString(R.string.status_x_wins)
                } else {
                    context.getString(R.string.status_o_wins)
                }
                GameMode.COMPUTER -> if (game.state.winner == game.playerMark) {
                    context.getString(R.string.status_you_win)
                } else {
                    context.getString(R.string.status_computer_wins)
                }
            }
            GameStatus.DRAW -> context.getString(R.string.status_draw)
            GameStatus.IN_PROGRESS -> ""
        }

        if (game.state.isOver && lastAnnouncedMoveCount != game.state.moveCount) {
            lastAnnouncedMoveCount = game.state.moveCount
            val message = when (game.state.status) {
                GameStatus.WIN -> if (game.state.winner == Mark.X) {
                    context.getString(R.string.announce_win_x)
                } else {
                    context.getString(R.string.announce_win_o)
                }
                GameStatus.DRAW -> context.getString(R.string.announce_draw)
                GameStatus.IN_PROGRESS -> ""
            }
            post { announceForAccessibility(message) }
        }
    }

    private fun difficultyLabel(difficulty: Difficulty): String = when (difficulty) {
        Difficulty.EASY -> context.getString(R.string.difficulty_easy)
        Difficulty.MEDIUM -> context.getString(R.string.difficulty_medium)
        Difficulty.HARD -> context.getString(R.string.difficulty_hard)
    }

    private fun showSetup() {
        selectedMark = viewModel.uiState.value.game?.playerMark ?: Mark.X
        selectedDifficulty = viewModel.uiState.value.settings.difficulty
        setupDifficulty.setOptions(
            listOf(
                context.getString(R.string.difficulty_easy),
                context.getString(R.string.difficulty_medium),
                context.getString(R.string.difficulty_hard)
            ),
            selectedDifficulty.ordinal
        )
        updateSetupSelection()
        setupVisible = true
        setupOverlay.visibility = View.VISIBLE
        setupOverlay.alpha = 0f
        setupCard.scaleX = 0.92f
        setupCard.scaleY = 0.92f
        setupOverlay.animate().alpha(1f).setDuration(220L).start()
        setupCard.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(360L)
            .setInterpolator(OvershootInterpolator(1.3f))
            .start()
    }

    private fun hideSetup() {
        if (!setupVisible) return
        setupVisible = false
        setupOverlay.animate()
            .alpha(0f)
            .setDuration(180L)
            .withEndAction { setupOverlay.visibility = View.GONE }
            .start()
    }

    private fun updateSetupSelection() {
        setupX.setSelectedState(selectedMark == Mark.X)
        setupO.setSelectedState(selectedMark == Mark.O)
        setupDifficulty.setSelected(selectedDifficulty.ordinal)
    }

    override fun onBackPressed(): Boolean {
        if (setupVisible) {
            hideSetup()
            navigator.goHome()
            return true
        }
        navigator.goHome()
        return true
    }

    override fun onHidden() {
        thinking.setActive(false)
        board.setInteractionEnabled(false)
    }
}
