package com.aksharanvay.xolo.data

import com.aksharanvay.xolo.game.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {

    @Test
    fun defaultsAreSensible() {
        val settings = SettingsRepository(InMemoryStore()).load()
        assertTrue(settings.soundEnabled)
        assertTrue(settings.vibrationEnabled)
        assertEquals(ThemeMode.SYSTEM, settings.theme)
        assertEquals(Difficulty.HARD, settings.difficulty)
    }

    @Test
    fun settingsSurviveAReload() {
        val store = InMemoryStore()
        val repository = SettingsRepository(store)
        repository.save(
            XoloSettings(
                soundEnabled = false,
                vibrationEnabled = false,
                theme = ThemeMode.DARK,
                difficulty = Difficulty.EASY
            )
        )

        val reloaded = SettingsRepository(store).load()
        assertFalse(reloaded.soundEnabled)
        assertFalse(reloaded.vibrationEnabled)
        assertEquals(ThemeMode.DARK, reloaded.theme)
        assertEquals(Difficulty.EASY, reloaded.difficulty)
    }

    @Test
    fun invalidStoredEnumsFallBackToDefaults() {
        val store = InMemoryStore(
            strings = mutableMapOf(
                "theme" to "NEON",
                "difficulty" to "IMPOSSIBLE"
            )
        )
        val settings = SettingsRepository(store).load()
        assertEquals(ThemeMode.SYSTEM, settings.theme)
        assertEquals(Difficulty.HARD, settings.difficulty)
    }
}
