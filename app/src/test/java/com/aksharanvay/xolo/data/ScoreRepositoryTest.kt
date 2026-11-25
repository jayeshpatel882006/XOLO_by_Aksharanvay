package com.aksharanvay.xolo.data

import com.aksharanvay.xolo.game.GameMode
import com.aksharanvay.xolo.game.Mark
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreRepositoryTest {

    @Test
    fun startsAtZero() {
        assertEquals(XoloScores(), ScoreRepository(InMemoryStore()).load())
    }

    @Test
    fun friendScoresAccumulateAndPersist() {
        val store = InMemoryStore()
        val repository = ScoreRepository(store)
        repository.recordFriendResult(Mark.X)
        repository.recordFriendResult(Mark.X)
        repository.recordFriendResult(Mark.O)
        repository.recordFriendResult(null)

        val reloaded = ScoreRepository(store).load()
        assertEquals(2, reloaded.friendX)
        assertEquals(1, reloaded.friendO)
        assertEquals(1, reloaded.friendDraws)
        assertEquals(0, reloaded.computerPlayer)
    }

    @Test
    fun computerScoresAccumulateAndPersist() {
        val store = InMemoryStore()
        val repository = ScoreRepository(store)
        repository.recordComputerResult(true)
        repository.recordComputerResult(false)
        repository.recordComputerResult(null)

        val reloaded = ScoreRepository(store).load()
        assertEquals(1, reloaded.computerPlayer)
        assertEquals(1, reloaded.computerAi)
        assertEquals(1, reloaded.computerDraws)
        assertEquals(0, reloaded.friendX)
    }

    @Test
    fun resetAllClearsBothModes() {
        val repository = ScoreRepository(InMemoryStore())
        repository.recordFriendResult(Mark.X)
        repository.recordComputerResult(true)
        val empty = repository.resetAll()
        assertEquals(XoloScores(), empty)
        assertEquals(XoloScores(), repository.load())
    }

    @Test
    fun resetOnlyTouchesTheRequestedMode() {
        val repository = ScoreRepository(InMemoryStore())
        repository.recordFriendResult(Mark.X)
        repository.recordFriendResult(Mark.O)
        repository.recordComputerResult(true)
        repository.recordComputerResult(false)

        val afterFriendReset = repository.reset(GameMode.FRIEND)
        assertEquals(0, afterFriendReset.friendX)
        assertEquals(0, afterFriendReset.friendO)
        assertEquals(1, afterFriendReset.computerPlayer)
        assertEquals(1, afterFriendReset.computerAi)

        val afterComputerReset = repository.reset(GameMode.COMPUTER)
        assertEquals(0, afterComputerReset.computerPlayer)
        assertEquals(0, afterComputerReset.computerAi)
        assertEquals(0, afterComputerReset.friendX)
    }

    @Test
    fun modeViewMapsLabelsCorrectly() {
        val scores = XoloScores(
            friendX = 1,
            friendO = 2,
            friendDraws = 3,
            computerPlayer = 4,
            computerAi = 5,
            computerDraws = 6
        )
        assertEquals(XoloScores.ModeScores(1, 3, 2), scores.forMode(GameMode.FRIEND))
        assertEquals(XoloScores.ModeScores(4, 6, 5), scores.forMode(GameMode.COMPUTER))
    }
}
