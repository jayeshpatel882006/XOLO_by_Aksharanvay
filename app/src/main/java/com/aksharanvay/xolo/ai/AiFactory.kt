package com.aksharanvay.xolo.ai

import com.aksharanvay.xolo.game.Difficulty
import kotlin.random.Random

object AiFactory {
    fun create(difficulty: Difficulty, random: Random = Random.Default): XoloAi = when (difficulty) {
        Difficulty.EASY -> EasyAi(random)
        Difficulty.MEDIUM -> MediumAi(random)
        Difficulty.HARD -> HardAi()
    }
}
