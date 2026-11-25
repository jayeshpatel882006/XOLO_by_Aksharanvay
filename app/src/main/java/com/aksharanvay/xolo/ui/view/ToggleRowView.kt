package com.aksharanvay.xolo.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.aksharanvay.xolo.R

class ToggleRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val titleView: TextView
    private val subtitleView: TextView
    private val toggle: XoloSwitch

    init {
        setBackgroundResource(R.drawable.bg_setting_row)
        elevation = context.dp(2f)
        LayoutInflater.from(context).inflate(R.layout.view_setting_toggle, this, true)
        titleView = findViewById(R.id.toggle_title)
        subtitleView = findViewById(R.id.toggle_subtitle)
        toggle = findViewById(R.id.toggle_switch)
        isClickable = true
        isFocusable = true
        setOnClickListener { toggle.toggle() }
    }

    fun bind(
        title: String,
        subtitle: String,
        checked: Boolean,
        onChange: (Boolean) -> Unit
    ) {
        titleView.text = title
        subtitleView.text = subtitle
        toggle.setOnCheckedChangeListener(null)
        toggle.isChecked = checked
        toggle.contentDescription = "$title, ${if (checked) context.getString(R.string.cd_toggle_on) else context.getString(R.string.cd_toggle_off)}"
        toggle.setOnCheckedChangeListener { _, isChecked ->
            toggle.contentDescription = "$title, ${if (isChecked) context.getString(R.string.cd_toggle_on) else context.getString(R.string.cd_toggle_off)}"
            onChange(isChecked)
        }
    }
}
