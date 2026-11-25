package com.aksharanvay.xolo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

enum class Sfx(val asset: String) {
    TAP("sounds/tap.wav"),
    PLACE_X("sounds/place_x.wav"),
    PLACE_O("sounds/place_o.wav"),
    WIN("sounds/win.wav"),
    DRAW("sounds/draw.wav"),
    START("sounds/start.wav")
}

class SoundManager(context: Context) {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids = mutableMapOf<Sfx, Int>()
    private var released = false

    init {
        Sfx.values().forEach { sfx ->
            runCatching {
                context.assets.openFd(sfx.asset).use { descriptor ->
                    ids[sfx] = pool.load(descriptor, 1)
                }
            }
        }
    }

    fun play(sfx: Sfx, volume: Float = 0.85f) {
        if (released) return
        val id = ids[sfx] ?: return
        pool.play(id, volume, volume, 1, 0, 1f)
    }

    fun release() {
        if (released) return
        released = true
        pool.release()
    }
}
