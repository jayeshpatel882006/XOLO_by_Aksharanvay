package com.aksharanvay.xolo.ui.screen

import android.content.Context
import android.widget.ImageButton
import android.widget.TextView
import com.aksharanvay.xolo.BuildConfig
import com.aksharanvay.xolo.R
import com.aksharanvay.xolo.XoloViewModel
import com.aksharanvay.xolo.data.ThemeMode
import com.aksharanvay.xolo.game.Difficulty
import com.aksharanvay.xolo.ui.XoloNavigator
import com.aksharanvay.xolo.ui.state.XoloUiState
import com.aksharanvay.xolo.ui.view.AmbientPlaygroundView
import com.aksharanvay.xolo.ui.view.SegmentedControl
import com.aksharanvay.xolo.ui.view.TactileButton
import com.aksharanvay.xolo.ui.view.ToggleRowView
import com.aksharanvay.xolo.ui.view.XoloDialog
import com.aksharanvay.xolo.ui.view.XoloPalette

class SettingsScreen(
    context: Context,
    private val viewModel: XoloViewModel,
    private val navigator: XoloNavigator
) : XoloScreen(context, R.layout.screen_settings) {

    private val palette = XoloPalette.from(context)
    private val ambient = findViewById<AmbientPlaygroundView>(R.id.settings_ambient)
    private val sound = findViewById<ToggleRowView>(R.id.settings_sound)
    private val vibration = findViewById<ToggleRowView>(R.id.settings_vibration)
    private val theme = findViewById<SegmentedControl>(R.id.settings_theme)
    private val difficulty = findViewById<SegmentedControl>(R.id.settings_difficulty)
    private val reset = findViewById<TactileButton>(R.id.settings_reset)
    private val resetSub = findViewById<TextView>(R.id.settings_reset_sub)
    private val version = findViewById<TextView>(R.id.settings_version)
    private var resetMessageRunnable: Runnable? = null

    init {
        ambient.setIntensity(0.38f)
        findViewById<ImageButton>(R.id.settings_back).setOnClickListener {
            navigator.tapFeedback()
            navigator.goHome()
        }
        theme.setAccent(palette.lilac)
        difficulty.setAccent(palette.mint)
        reset.setAction(context.getString(R.string.reset_scores), palette.solar, R.drawable.ic_restart)
        version.text = context.getString(R.string.version_format, BuildConfig.VERSION_NAME)

        reset.setOnClickListener {
            navigator.tapFeedback()
            XoloDialog(context).show(
                title = context.getString(R.string.reset_scores_dialog_title),
                message = context.getString(R.string.reset_scores_dialog_body),
                confirmLabel = context.getString(R.string.reset),
                cancelLabel = context.getString(R.string.cancel)
            ) {
                viewModel.resetEverything()
                showResetMessage()
                postDelayed({ navigator.goHome() }, 620L)
            }
        }
    }

    override fun render(state: XoloUiState) {
        sound.bind(
            context.getString(R.string.sound_title),
            context.getString(R.string.sound_sub),
            state.settings.soundEnabled
        ) { enabled ->
            viewModel.setSoundEnabled(enabled)
            navigator.tapFeedback()
        }

        vibration.bind(
            context.getString(R.string.vibration_title),
            context.getString(R.string.vibration_sub),
            state.settings.vibrationEnabled
        ) { enabled ->
            viewModel.setVibrationEnabled(enabled)
            navigator.tapFeedback()
        }

        theme.setOptions(
            listOf(
                context.getString(R.string.theme_system),
                context.getString(R.string.theme_light),
                context.getString(R.string.theme_dark)
            ),
            state.settings.theme.ordinal
        )
        theme.onSelected = { index ->
            viewModel.setTheme(ThemeMode.values()[index])
            navigator.applyTheme()
        }

        difficulty.setOptions(
            listOf(
                context.getString(R.string.difficulty_easy),
                context.getString(R.string.difficulty_medium),
                context.getString(R.string.difficulty_hard)
            ),
            state.settings.difficulty.ordinal
        )
        difficulty.onSelected = { index ->
            viewModel.setDifficulty(Difficulty.values()[index])
            navigator.tapFeedback()
        }
    }

    private fun showResetMessage() {
        resetMessageRunnable?.let { removeCallbacks(it) }
        resetSub.text = context.getString(R.string.reset_done)
        val runnable = Runnable {
            resetSub.text = context.getString(R.string.reset_scores_sub)
        }
        resetMessageRunnable = runnable
        postDelayed(runnable, 2200L)
    }

    override fun onBackPressed(): Boolean {
        navigator.goHome()
        return true
    }

    override fun onDetachedFromWindow() {
        resetMessageRunnable?.let { removeCallbacks(it) }
        super.onDetachedFromWindow()
    }
}
