package com.aksharanvay.xolo.game

/**
 * The two marks in XOLO. X always moves first in a fresh game.
 */
enum class Mark {
    X,
    O;

    fun other(): Mark = if (this == X) O else X
}
