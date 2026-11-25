package com.aksharanvay.xolo.ui.screen

import android.content.Context
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.aksharanvay.xolo.BuildConfig
import com.aksharanvay.xolo.R
import com.aksharanvay.xolo.ui.XoloNavigator
import com.aksharanvay.xolo.ui.view.AmbientPlaygroundView

class AboutScreen(
    context: Context,
    private val navigator: XoloNavigator
) : XoloScreen(context, R.layout.screen_about) {

    private val ambient = findViewById<AmbientPlaygroundView>(R.id.about_ambient)
    private val logo = findViewById<ImageView>(R.id.about_logo)
    private val publisher = findViewById<View>(R.id.about_publisher)
    private val version = findViewById<TextView>(R.id.about_version)

    init {
        ambient.setIntensity(0.5f)
        version.text = context.getString(R.string.version_format, BuildConfig.VERSION_NAME)
        findViewById<ImageButton>(R.id.about_back).setOnClickListener {
            navigator.tapFeedback()
            navigator.goHome()
        }
    }

    override fun onShown() {
        logo.alpha = 0f
        logo.scaleX = 0.7f
        logo.scaleY = 0.7f
        logo.rotation = -12f
        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .rotation(0f)
            .setDuration(680L)
            .setInterpolator(OvershootInterpolator(1.4f))
            .start()

        publisher.alpha = 0f
        publisher.scaleX = 0.86f
        publisher.scaleY = 0.86f
        publisher.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(220L)
            .setDuration(560L)
            .setInterpolator(OvershootInterpolator(1.3f))
            .start()
    }

    override fun onBackPressed(): Boolean {
        navigator.goHome()
        return true
    }
}
