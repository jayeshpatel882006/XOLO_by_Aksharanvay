package com.aksharanvay.xolo.ui.screen

import android.content.Context
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import com.aksharanvay.xolo.R
import com.aksharanvay.xolo.game.Mark
import com.aksharanvay.xolo.ui.XoloNavigator
import com.aksharanvay.xolo.ui.view.AmbientPlaygroundView
import com.aksharanvay.xolo.ui.view.TactileButton
import com.aksharanvay.xolo.ui.view.XoloPalette
import com.aksharanvay.xolo.ui.view.dp

class HomeScreen(
    context: Context,
    private val navigator: XoloNavigator
) : XoloScreen(context, R.layout.screen_home) {

    private val palette = XoloPalette.from(context)
    private val ambient = findViewById<AmbientPlaygroundView>(R.id.home_ambient)
    private val wordmark = findViewById<TextView>(R.id.home_wordmark)
    private val signature = findViewById<TextView>(R.id.home_signature)
    private val tagline = findViewById<TextView>(R.id.home_tagline)
    private val friend = findViewById<TactileButton>(R.id.home_friend)
    private val computer = findViewById<TactileButton>(R.id.home_computer)
    private val settings = findViewById<TactileButton>(R.id.home_settings)
    private val about = findViewById<TactileButton>(R.id.home_about)
    private val footer = findViewById<TextView>(R.id.home_footer)

    init {
        ambient.setIntensity(1f)
        ambient.onPlayfulTap = { navigator.tapFeedback() }

        friend.setMode(
            context.getString(R.string.play_friend_title),
            context.getString(R.string.play_friend_sub),
            Mark.X,
            palette.solar
        )
        computer.setMode(
            context.getString(R.string.play_computer_title),
            context.getString(R.string.play_computer_sub),
            Mark.O,
            palette.mint
        )
        settings.setAction(context.getString(R.string.settings), palette.lilac, R.drawable.ic_tune)
        about.setAction(context.getString(R.string.about), palette.butter, R.drawable.ic_info)

        friend.setOnClickListener {
            navigator.tapFeedback()
            navigator.playFriend()
        }
        computer.setOnClickListener {
            navigator.tapFeedback()
            navigator.playComputer()
        }
        settings.setOnClickListener {
            navigator.tapFeedback()
            navigator.goSettings()
        }
        about.setOnClickListener {
            navigator.tapFeedback()
            navigator.goAbout()
        }
    }

    override fun onShown() {
        listOf(wordmark, signature, tagline, friend, computer, settings, about, footer)
            .forEach { it.animate().cancel() }

        animateText(wordmark, 70L)
        animateText(signature, 120L)
        animateText(tagline, 170L)
        animateCard(friend, 260L)
        animateCard(computer, 340L)
        animateCard(settings, 430L)
        animateCard(about, 480L)
        animateText(footer, 560L)
    }

    private fun animateText(view: View, delay: Long) {
        view.alpha = 0f
        view.translationY = context.dp(10f)
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(delay)
            .setDuration(460L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun animateCard(view: View, delay: Long) {
        view.alpha = 0f
        view.translationY = context.dp(22f)
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(delay)
            .setDuration(540L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
}
