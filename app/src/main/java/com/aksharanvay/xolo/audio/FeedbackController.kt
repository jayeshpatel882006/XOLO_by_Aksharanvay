package com.aksharanvay.xolo.audio

import android.content.Context
import com.aksharanvay.xolo.data.XoloSettings
import com.aksharanvay.xolo.game.Mark
import com.aksharanvay.xolo.ui.state.GameEvent

class FeedbackController(context: Context) {

    private val sounds = SoundManager(context)
    private val haptics = Haptics(context)

    fun tap(settings: XoloSettings) {
        if (settings.soundEnabled) sounds.play(Sfx.TAP)
        if (settings.vibrationEnabled) haptics.tap()
    }

    fun gameEvent(event: GameEvent, settings: XoloSettings) {
        when (event) {
            is GameEvent.Placed -> {
                if (settings.soundEnabled) {
                    sounds.play(if (event.mark == Mark.X) Sfx.PLACE_X else Sfx.PLACE_O)
                }
                if (settings.vibrationEnabled) haptics.tap()
            }
            is GameEvent.Won -> {
                if (settings.soundEnabled) sounds.play(Sfx.WIN)
                if (settings.vibrationEnabled) haptics.success()
            }
            GameEvent.Drawn -> {
                if (settings.soundEnabled) sounds.play(Sfx.DRAW)
                if (settings.vibrationEnabled) haptics.soft()
            }
            GameEvent.Started -> {
                if (settings.soundEnabled) sounds.play(Sfx.START, 0.65f)
            }
        }
    }

    fun release() {
        sounds.release()
    }
}
