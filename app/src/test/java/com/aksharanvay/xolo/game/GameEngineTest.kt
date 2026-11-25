package com.aksharanvay.xolo.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    private fun play(state: GameState, vararg moves: Int): GameState {
        var current = state
        for (move in moves) {
            val outcome = GameEngine.play(current, move)
            assertTrue("Move $move should be accepted", outcome is MoveOutcome.Accepted)
            current = (outcome as MoveOutcome.Accepted).state
        }
        return current
    }

    @Test
    fun newGameIsEmptyAndXStarts() {
        val state = GameEngine.newGame()
        assertEquals(9, state.board.size)
        assertTrue(state.board.all { it == null })
        assertEquals(Mark.X, state.currentTurn)
        assertEquals(GameStatus.IN_PROGRESS, state.status)
        assertEquals(0, state.moveCount)
        assertNull(state.winner)
        assertNull(state.winningLine)
    }

    @Test
    fun turnsAlternate() {
        var state = GameEngine.newGame()
        state = play(state, 0)
        assertEquals(Mark.O, state.currentTurn)
        state = play(state, 1)
        assertEquals(Mark.X, state.currentTurn)
        state = play(state, 2)
        assertEquals(Mark.O, state.currentTurn)
        assertEquals(Mark.X, state.board[0])
        assertEquals(Mark.O, state.board[1])
        assertEquals(Mark.X, state.board[2])
    }

    @Test
    fun xWinsOnEveryWinningLine() {
        for (line in GameEngine.WIN_LINES) {
            val fillers = (0..8).filter { it !in line }.take(2)
            var state = GameEngine.newGame(Mark.X)
            state = play(state, line[0])
            state = play(state, fillers[0])
            state = play(state, line[1])
            state = play(state, fillers[1])
            state = play(state, line[2])

            assertEquals("Line $line", GameStatus.WIN, state.status)
            assertEquals("Line $line", Mark.X, state.winner)
            assertEquals("Line $line", line, state.winningLine)
        }
    }

    @Test
    fun oWinsOnEveryWinningLine() {
        for (line in GameEngine.WIN_LINES) {
            val fillers = (0..8).filter { it !in line }.take(2)
            var state = GameEngine.newGame(Mark.O)
            state = play(state, line[0])
            state = play(state, fillers[0])
            state = play(state, line[1])
            state = play(state, fillers[1])
            state = play(state, line[2])

            assertEquals("Line $line", GameStatus.WIN, state.status)
            assertEquals("Line $line", Mark.O, state.winner)
            assertEquals("Line $line", line, state.winningLine)
        }
    }

    @Test
    fun fullBoardWithoutAWinnerIsADraw() {
        val state = play(GameEngine.newGame(), 0, 1, 2, 4, 3, 5, 7, 6, 8)
        assertEquals(GameStatus.DRAW, state.status)
        assertNull(state.winner)
        assertNull(state.winningLine)
        assertEquals(9, state.moveCount)
    }

    @Test
    fun occupiedCellIsRejected() {
        val state = play(GameEngine.newGame(), 4)
        val outcome = GameEngine.play(state, 4)
        assertTrue(outcome is MoveOutcome.Rejected)
        assertEquals(MoveError.CELL_OCCUPIED, (outcome as MoveOutcome.Rejected).reason)
    }

    @Test
    fun outOfRangeMovesAreRejected() {
        val state = GameEngine.newGame()
        assertEquals(
            MoveError.INVALID_INDEX,
            (GameEngine.play(state, -1) as MoveOutcome.Rejected).reason
        )
        assertEquals(
            MoveError.INVALID_INDEX,
            (GameEngine.play(state, 9) as MoveOutcome.Rejected).reason
        )
    }

    @Test
    fun noMovesAreAcceptedAfterAWin() {
        val won = play(GameEngine.newGame(), 0, 3, 1, 4, 2)
        assertEquals(GameStatus.WIN, won.status)
        val outcome = GameEngine.play(won, 8)
        assertEquals(MoveError.GAME_OVER, (outcome as MoveOutcome.Rejected).reason)
    }

    @Test
    fun availableMovesShrinkAndDisappear() {
        var state = GameEngine.newGame()
        assertEquals(9, GameEngine.availableMoves(state).size)
        state = play(state, 4)
        assertEquals(8, GameEngine.availableMoves(state).size)
        assertTrue(4 !in GameEngine.availableMoves(state))
        val won = play(GameEngine.newGame(), 0, 3, 1, 4, 2)
        assertTrue(GameEngine.availableMoves(won).isEmpty())
    }

    @Test
    fun aFreshGameIsACleanRestart() {
        val played = play(GameEngine.newGame(), 0, 1, 2)
        val restarted = GameEngine.newGame()
        assertEquals(0, restarted.moveCount)
        assertTrue(restarted.board.all { it == null })
        assertTrue(played.moveCount > restarted.moveCount)
    }

    @Test
    fun findWinRecognisesEveryLine() {
        for (line in GameEngine.WIN_LINES) {
            val board = MutableList<Mark?>(9) { null }
            line.forEach { board[it] = Mark.X }
            val result = GameEngine.findWin(board)
            assertEquals(Mark.X, result?.first)
            assertEquals(line, result?.second)
        }
    }
}
