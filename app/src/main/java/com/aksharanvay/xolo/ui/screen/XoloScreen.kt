package com.aksharanvay.xolo.ui.screen

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import com.aksharanvay.xolo.ui.state.XoloUiState

abstract class XoloScreen(
    context: Context,
    @LayoutRes layout: Int
) : FrameLayout(context) {

    init {
        LayoutInflater.from(context).inflate(layout, this, true)
    }

    open fun onShown() = Unit

    open fun onHidden() = Unit

    open fun render(state: XoloUiState) = Unit

    /**
     * Return true when the screen consumed the back press.
     */
    open fun onBackPressed(): Boolean = false
}
