package com.aksharanvay.xolo.ui.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import com.aksharanvay.xolo.R

/**
 * A deliberately small confirmation surface, styled with the same tactile
 * language as the rest of XOLO.
 */
class XoloDialog(private val host: Context) {

    fun show(
        title: String,
        message: String,
        confirmLabel: String,
        cancelLabel: String,
        onConfirm: () -> Unit
    ) {
        val dialog = Dialog(host)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(LayoutInflater.from(host).inflate(R.layout.dialog_xolo, null))
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.55f)
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
            setWindowAnimations(0)
        }
        dialog.setCanceledOnTouchOutside(true)

        val card = dialog.findViewById<LinearLayout>(R.id.dialog_card)
        dialog.findViewById<TextView>(R.id.dialog_title).text = title
        dialog.findViewById<TextView>(R.id.dialog_message).text = message

        val palette = XoloPalette.from(host)
        val cancel = dialog.findViewById<TactileButton>(R.id.dialog_cancel)
        val confirm = dialog.findViewById<TactileButton>(R.id.dialog_confirm)
        cancel.setAction(cancelLabel, palette.textSecondary)
        confirm.setAction(confirmLabel, palette.solar)

        cancel.setOnClickListener { dialog.dismiss() }
        confirm.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }

        dialog.setOnShowListener {
            card.alpha = 0f
            card.scaleX = 0.9f
            card.scaleY = 0.9f
            card.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(240L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        dialog.show()
    }
}
