package com.aksharanvay.xolo.ui.view

import android.content.Context
import android.util.TypedValue
import androidx.annotation.ColorInt
import com.aksharanvay.xolo.R

/**
 * Theme-aware palette resolved from custom attributes. Every custom view reads
 * colours through this class, so changing Theme.Xolo.Light / Dark updates the
 * entire app (including Canvas-drawn surfaces).
 */
data class XoloPalette(
    @ColorInt val paper: Int,
    @ColorInt val paperDeep: Int,
    @ColorInt val surface: Int,
    @ColorInt val surfaceAlt: Int,
    @ColorInt val ink: Int,
    @ColorInt val textPrimary: Int,
    @ColorInt val textSecondary: Int,
    @ColorInt val solar: Int,
    @ColorInt val solarDeep: Int,
    @ColorInt val mint: Int,
    @ColorInt val mintDeep: Int,
    @ColorInt val lilac: Int,
    @ColorInt val butter: Int,
    @ColorInt val boardWell: Int,
    @ColorInt val boardWellEdge: Int,
    @ColorInt val shadow: Int,
    @ColorInt val stroke: Int,
    @ColorInt val overlay: Int
) {
    companion object {
        fun from(context: Context): XoloPalette {
            val theme = context.theme
            fun attr(id: Int): Int {
                val value = TypedValue()
                if (!theme.resolveAttribute(id, value, true)) return 0
                return if (value.resourceId != 0) {
                    context.resources.getColor(value.resourceId, theme)
                } else {
                    value.data
                }
            }
            return XoloPalette(
                paper = attr(R.attr.xoloPaper),
                paperDeep = attr(R.attr.xoloPaperDeep),
                surface = attr(R.attr.xoloSurface),
                surfaceAlt = attr(R.attr.xoloSurfaceAlt),
                ink = attr(R.attr.xoloInk),
                textPrimary = attr(R.attr.xoloTextPrimary),
                textSecondary = attr(R.attr.xoloTextSecondary),
                solar = attr(R.attr.xoloSolar),
                solarDeep = attr(R.attr.xoloSolarDeep),
                mint = attr(R.attr.xoloMint),
                mintDeep = attr(R.attr.xoloMintDeep),
                lilac = attr(R.attr.xoloLilac),
                butter = attr(R.attr.xoloButter),
                boardWell = attr(R.attr.xoloBoardWell),
                boardWellEdge = attr(R.attr.xoloBoardWellEdge),
                shadow = attr(R.attr.xoloShadow),
                stroke = attr(R.attr.xoloStroke),
                overlay = attr(R.attr.xoloOverlay)
            )
        }
    }
}
