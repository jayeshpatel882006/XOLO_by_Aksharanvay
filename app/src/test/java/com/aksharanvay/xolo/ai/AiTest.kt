package com.aksharanvay.xolo.ai

import com.aksharanvay.xolo.game.GameEngine
import com.aksharanvay.xolo.game.GameState
import com.aksharanvay.xolo.game.GameStatus
import com.aksharanvay.xolo.game.Mark
import com.aksharanvay.xolo.game.MoveOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AiTest {

    private fun play(state: GameState, vararg moves: Int): GameState {
        var current = state
        for (move in moves) {
            current = (GameEngine.play(current, move) as MoveOutcome.Accepted).state
        }
        return current
    }

    private fun stateWithOThreatAndOTurn(): GameState =
        play(GameEngine.newGame(Mark.X), 3, 0, 4, 1, 8)

    private fun stateWithOThreatAndXTurn(): GameState =
        play(GameEngine.newGame(Mark.X), 0, 3, 8, 4)

    @Test
    fun easyAiAlwaysPicksALegalSquare() {
        val ai = EasyAi(Random(7))
        val states = listOf(
            GameEngine.newGame(),
            play(GameEngine.newGame(), 4),
            play(GameEngine.newGame(), 0, 1, 2, 3),
            play(GameEngine.newGame(), 0, 1, 2, 4, 3, 5, 7)
        )
        for (state in states) {
            val move = ai.chooseMove(state)
            assertNotNull(move)
            assertTrue("Move $move must be empty", state.board[move!!] == null)
        }
    }

    @Test
    fun easyAiReturnsNullOnAFinishedBoard() {
        val finished = play(GameEngine.newGame(), 0, 3, 1, 4, 2)
        assertEquals(GameStatus.WIN, finished.status)
        assertNull(EasyAi(Random(1)).chooseMove(finished))
    }

    @Test
    fun mediumAiTakesAnImmediateWin() {
        val state = stateWithOThreatAndOTurn()
        assertEquals(2, MediumAi(Random(2)).chooseMove(state))
    }

    @Test
    fun mediumAiBlocksAnImmediateLoss() {
        val state = stateWithOThreatAndXTurn()
        assertEquals(5, MediumAi(Random(2)).chooseMove(state))
    }

    @Test
    fun mediumAiTakesTheCentreOnAnEmptyBoard() {
        assertEquals(4, MediumAi(Random(3)).chooseMove(GameEngine.newGame()))
    }

    @Test
    fun mediumAiNeverPicksAnOccupiedSquare() {
        val ai = MediumAi(Random(4))
        val state = play(GameEngine.newGame(), 0, 1, 4, 8, 2)
        repeat(30) {
            val move = ai.chooseMove(state)
            assertNotNull(move)
            assertTrue(state.board[move!!] == null)
        }
    }

    @Test
    fun hardAiTakesAnImmediateWin() {
        val state = stateWithOThreatAndOTurn()
        assertEquals(2, HardAi().chooseMove(state))
    }

    @Test
    fun hardAiBlocksAnImmediateLoss() {
        val state = stateWithOThreatAndXTurn()
        assertEquals(5, HardAi().chooseMove(state))
    }

    @Test
    fun hardAiTakesTheCentreOnAnEmptyBoard() {
        assertEquals(4, HardAi().chooseMove(GameEngine.newGame()))
    }

    @Test
    fun hardAiReturnsNullWhenTheBoardIsFull() {
        val finished = play(GameEngine.newGame(), 0, 1, 2, 4, 3, 5, 7, 6, 8)
        assertEquals(GameStatus.DRAW, finished.status)
        assertNull(HardAi().chooseMove(finished))
    }

    @Test
    fun hardAiNeverLosesAsOAgainstEveryHumanReply() {
        assertTrue(exploreAllHumanReplies(HardAi(), Mark.O))
    }

    @Test
    fun hardAiNeverLosesAsXAgainstEveryHumanReply() {
        assertTrue(exploreAllHumanReplies(HardAi(), Mark.X))
    }

    /**
     * Exhaustively explores every human move while the AI plays its chosen move.
     * Returns true when the AI avoids losing in every branch.
     */
    private fun exploreAllHumanReplies(ai: HardAi, aiMark: Mark): Boolean {
        fun explore(state: GameState): Boolean {
            if (state.status != GameStatus.IN_PROGRESS) {
                return state.winner != aiMark.other()
            }
            if (state.currentTurn == aiMark) {
                val move = ai.chooseMove(state) ?: return false
                val next = (GameEngine.play(state, move) as MoveOutcome.Accepted).state
                return explore(next)
            }
            for (move in GameEngine.availableMoves(state)) {
                val next = (GameEngine.play(state, move) as MoveOutcome.Accepted).state
                if (!explore(next)) return false
            }
            return true
        }
        return explore(GameEngine.newGame(Mark.X))
    }
}
