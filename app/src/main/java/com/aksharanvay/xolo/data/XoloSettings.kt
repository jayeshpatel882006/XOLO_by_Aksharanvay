package com.aksharanvay.xolo.data

import com.aksharanvay.xolo.game.Difficulty

data class XoloSettings(
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val difficulty: Difficulty = Difficulty.HARD
)
