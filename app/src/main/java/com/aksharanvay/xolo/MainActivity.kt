package com.aksharanvay.xolo

import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aksharanvay.xolo.audio.FeedbackController
import com.aksharanvay.xolo.data.ScoreRepository
import com.aksharanvay.xolo.data.SettingsRepository
import com.aksharanvay.xolo.data.SharedPreferencesStore
import com.aksharanvay.xolo.data.ThemeMode
import com.aksharanvay.xolo.game.GameMode
import com.aksharanvay.xolo.ui.XoloNavigator
import com.aksharanvay.xolo.ui.screen.AboutScreen
import com.aksharanvay.xolo.ui.screen.GameScreen
import com.aksharanvay.xolo.ui.screen.HomeScreen
import com.aksharanvay.xolo.ui.screen.SettingsScreen
import com.aksharanvay.xolo.ui.screen.SplashScreen
import com.aksharanvay.xolo.ui.screen.XoloScreen
import com.aksharanvay.xolo.ui.view.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), XoloNavigator {

    private lateinit var viewModel: XoloViewModel
    private lateinit var feedback: FeedbackController
    private lateinit var host: FrameLayout

    private var currentScreen: XoloScreen? = null
    private var screenKey: String = KEY_HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        val settingsPreferences = applicationContext.getSharedPreferences(
            SETTINGS_PREFS,
            MODE_PRIVATE
        )
        val settingsRepository = SettingsRepository(SharedPreferencesStore(settingsPreferences))
        val settings = settingsRepository.load()
        val dark = when (settings.theme) {
            ThemeMode.SYSTEM -> isSystemNight()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
        setTheme(if (dark) R.style.Theme_Xolo_Dark else R.style.Theme_Xolo_Light)

        super.onCreate(savedInstanceState)

        val scoreRepository = ScoreRepository(
            SharedPreferencesStore(getSharedPreferences(SCORE_PREFS, MODE_PRIVATE))
        )
        viewModel = ViewModelProvider(
            this,
            XoloViewModel.Factory(settingsRepository, scoreRepository)
        )[XoloViewModel::class.java]
        feedback = FeedbackController(this)

        setContentView(R.layout.activity_main)
        host = findViewById(R.id.screen_host)

        onBackPressedDispatcher.addCallback(this) {
            val screen = currentScreen
            if (screen != null && screen.onBackPressed()) return@addCallback
            if (screenKey == KEY_HOME || screen == null) {
                finish()
            } else {
                goHome()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        currentScreen?.render(state)
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        feedback.gameEvent(event, viewModel.uiState.value.settings)
                    }
                }
            }
        }

        if (savedInstanceState == null && !viewModel.splashShown) {
            showSplash()
        } else {
            restoreScreen(savedInstanceState?.getString(KEY_SCREEN) ?: KEY_HOME)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_SCREEN, screenKey)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        feedback.release()
        super.onDestroy()
    }

    private fun isSystemNight(): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    private fun showSplash() {
        val splash = SplashScreen(this) {
            viewModel.markSplashShown()
            if (screenKey == KEY_SPLASH) showHome()
        }
        showImmediate(splash, KEY_SPLASH)
    }

    private fun restoreScreen(key: String) {
        when (key) {
            KEY_GAME_FRIEND -> {
                if (viewModel.uiState.value.game?.mode == GameMode.FRIEND) {
                    openGame(GameMode.FRIEND)
                } else {
                    showHome()
                }
            }
            KEY_GAME_COMPUTER -> openGame(GameMode.COMPUTER)
            KEY_SETTINGS -> goSettings()
            KEY_ABOUT -> goAbout()
            KEY_SPLASH -> showSplash()
            else -> showHome()
        }
    }

    private fun showHome() {
        navigate(HomeScreen(this, this), KEY_HOME)
    }

    private fun openGame(mode: GameMode) {
        navigate(GameScreen(this, mode, viewModel, this), keyFor(mode))
    }

    private fun showImmediate(screen: XoloScreen, key: String) {
        host.removeAllViews()
        currentScreen = screen
        screenKey = key
        host.addView(
            screen,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        screen.onShown()
        screen.render(viewModel.uiState.value)
    }

    private fun navigate(screen: XoloScreen, key: String) {
        val old = currentScreen
        if (old != null) {
            old.onHidden()
            old.animate()
                .alpha(0f)
                .translationY(-dp(8f))
                .setDuration(150L)
                .withEndAction { host.removeView(old) }
                .start()
        }

        currentScreen = screen
        screenKey = key
        host.addView(
            screen,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        screen.alpha = 0f
        screen.translationY = dp(12f)
        screen.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(270L)
            .start()

        screen.onShown()
        screen.render(viewModel.uiState.value)
    }

    override fun goHome() {
        if (screenKey != KEY_HOME) showHome()
    }

    override fun goSettings() {
        navigate(SettingsScreen(this, viewModel, this), KEY_SETTINGS)
    }

    override fun goAbout() {
        navigate(AboutScreen(this, this), KEY_ABOUT)
    }

    override fun playFriend() {
        viewModel.startFriendGame()
        openGame(GameMode.FRIEND)
    }

    override fun playComputer() {
        viewModel.clearGame()
        openGame(GameMode.COMPUTER)
    }

    override fun tapFeedback() {
        feedback.tap(viewModel.uiState.value.settings)
    }

    override fun applyTheme() {
        recreate()
    }

    private fun keyFor(mode: GameMode): String = when (mode) {
        GameMode.FRIEND -> KEY_GAME_FRIEND
        GameMode.COMPUTER -> KEY_GAME_COMPUTER
    }

    private companion object {
        const val SETTINGS_PREFS = "xolo_settings"
        const val SCORE_PREFS = "xolo_scores"
        const val KEY_SCREEN = "xolo_screen"
        const val KEY_HOME = "home"
        const val KEY_SPLASH = "splash"
        const val KEY_GAME_FRIEND = "game_friend"
        const val KEY_GAME_COMPUTER = "game_computer"
        const val KEY_SETTINGS = "settings"
        const val KEY_ABOUT = "about"
    }
}
