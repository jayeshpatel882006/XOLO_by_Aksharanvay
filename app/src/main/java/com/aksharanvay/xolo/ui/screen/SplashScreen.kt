package com.aksharanvay.xolo.ui.screen

import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.aksharanvay.xolo.R
import com.aksharanvay.xolo.ui.view.AmbientPlaygroundView
import com.aksharanvay.xolo.ui.view.dp

class SplashScreen(
    context: Context,
    private val onFinished: () -> Unit
) : XoloScreen(context, R.layout.screen_splash) {

    private val ambient = findViewById<AmbientPlaygroundView>(R.id.splash_ambient)
    private val logo = findViewById<ImageView>(R.id.splash_logo)
    private val wordmark = findViewById<TextView>(R.id.splash_wordmark)
    private val signature = findViewById<TextView>(R.id.splash_signature)
    private val accent = findViewById<View>(R.id.splash_accent)
    private val tagline = findViewById<TextView>(R.id.splash_tagline)
    private val publisher = findViewById<View>(R.id.splash_publisher)

    private var started = false
    private var logoFloat: ValueAnimator? = null
    private val finishRunnable = Runnable { onFinished() }

    override fun onShown() {
        if (started) return
        started = true
        ambient.setIntensity(1f)
        wordmark.translationY = context.dp(14f)
        tagline.translationY = context.dp(10f)
        logo.rotation = -14f

        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .rotation(0f)
            .setStartDelay(80L)
            .setDuration(720L)
            .setInterpolator(OvershootInterpolator(1.5f))
            .withEndAction { startLogoFloat() }
            .start()

        wordmark.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(220L)
            .setDuration(700L)
            .setInterpolator(DecelerateInterpolator())
            .start()

        ValueAnimator.ofFloat(0.32f, 0.12f).apply {
            duration = 900L
            startDelay = 220L
            addUpdateListener { wordmark.letterSpacing = it.animatedValue as Float }
            start()
        }

        signature.animate()
            .alpha(1f)
            .setStartDelay(520L)
            .setDuration(420L)
            .start()

        accent.animate()
            .scaleX(1f)
            .setStartDelay(700L)
            .setDuration(480L)
            .setInterpolator(DecelerateInterpolator())
            .start()

        tagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(880L)
            .setDuration(520L)
            .start()

        publisher.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(1040L)
            .setDuration(560L)
            .setInterpolator(OvershootInterpolator(1.4f))
            .start()

        postDelayed(finishRunnable, SPLASH_DURATION)
    }

    /**
     * Keeps the new glossy mark alive after its entrance instead of letting it
     * sit still for the rest of the splash.
     */
    private fun startLogoFloat() {
        if (!isAttachedToWindow || logoFloat != null) return
        logoFloat = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1500L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val progress = it.animatedValue as Float
                logo.translationY = -context.dp(3f) * progress
                logo.rotation = 2.5f * progress
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(finishRunnable)
        logoFloat?.cancel()
        logoFloat = null
        super.onDetachedFromWindow()
    }

    private companion object {
        const val SPLASH_DURATION = 1900L
    }
}
