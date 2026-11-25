package com.aksharanvay.xolo.data

import com.aksharanvay.xolo.game.Difficulty

class SettingsRepository(private val store: KeyValueStore) {

    fun load(): XoloSettings = XoloSettings(
        soundEnabled = store.getBoolean(KEY_SOUND, true),
        vibrationEnabled = store.getBoolean(KEY_VIBRATION, true),
        theme = enumOrDefault(store.getString(KEY_THEME, ThemeMode.SYSTEM.name), ThemeMode.SYSTEM),
        difficulty = enumOrDefault(store.getString(KEY_DIFFICULTY, Difficulty.HARD.name), Difficulty.HARD)
    )

    fun save(settings: XoloSettings) {
        store.putBoolean(KEY_SOUND, settings.soundEnabled)
        store.putBoolean(KEY_VIBRATION, settings.vibrationEnabled)
        store.putString(KEY_THEME, settings.theme.name)
        store.putString(KEY_DIFFICULTY, settings.difficulty.name)
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T =
        runCatching { enumValueOf<T>(value ?: default.name) }.getOrDefault(default)

    private companion object {
        const val KEY_SOUND = "sound_enabled"
        const val KEY_VIBRATION = "vibration_enabled"
        const val KEY_THEME = "theme"
        const val KEY_DIFFICULTY = "difficulty"
    }
}
