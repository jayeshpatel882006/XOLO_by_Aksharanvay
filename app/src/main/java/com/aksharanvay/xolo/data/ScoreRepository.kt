package com.aksharanvay.xolo.data

import com.aksharanvay.xolo.game.Mark
import com.aksharanvay.xolo.game.GameMode

data class XoloScores(
    val friendX: Int = 0,
    val friendO: Int = 0,
    val friendDraws: Int = 0,
    val computerPlayer: Int = 0,
    val computerAi: Int = 0,
    val computerDraws: Int = 0
) {
    fun forMode(mode: GameMode): ModeScores = when (mode) {
        GameMode.FRIEND -> ModeScores(friendX, friendDraws, friendO)
        GameMode.COMPUTER -> ModeScores(computerPlayer, computerDraws, computerAi)
    }

    data class ModeScores(val left: Int, val draws: Int, val right: Int)
}

class ScoreRepository(private val store: KeyValueStore) {

    fun load(): XoloScores = XoloScores(
        friendX = store.getInt(KEY_FRIEND_X, 0),
        friendO = store.getInt(KEY_FRIEND_O, 0),
        friendDraws = store.getInt(KEY_FRIEND_DRAWS, 0),
        computerPlayer = store.getInt(KEY_COMPUTER_PLAYER, 0),
        computerAi = store.getInt(KEY_COMPUTER_AI, 0),
        computerDraws = store.getInt(KEY_COMPUTER_DRAWS, 0)
    )

    fun recordFriendResult(winner: Mark?): XoloScores {
        val current = load()
        val next = when (winner) {
            Mark.X -> current.copy(friendX = current.friendX + 1)
            Mark.O -> current.copy(friendO = current.friendO + 1)
            null -> current.copy(friendDraws = current.friendDraws + 1)
        }
        save(next)
        return next
    }

    fun recordComputerResult(playerWon: Boolean?): XoloScores {
        val current = load()
        val next = when (playerWon) {
            true -> current.copy(computerPlayer = current.computerPlayer + 1)
            false -> current.copy(computerAi = current.computerAi + 1)
            null -> current.copy(computerDraws = current.computerDraws + 1)
        }
        save(next)
        return next
    }

    fun resetAll(): XoloScores {
        val empty = XoloScores()
        save(empty)
        return empty
    }

    fun reset(mode: GameMode): XoloScores {
        val current = load()
        val next = when (mode) {
            GameMode.FRIEND -> current.copy(friendX = 0, friendO = 0, friendDraws = 0)
            GameMode.COMPUTER -> current.copy(
                computerPlayer = 0,
                computerAi = 0,
                computerDraws = 0
            )
        }
        save(next)
        return next
    }

    private fun save(scores: XoloScores) {
        store.putInt(KEY_FRIEND_X, scores.friendX)
        store.putInt(KEY_FRIEND_O, scores.friendO)
        store.putInt(KEY_FRIEND_DRAWS, scores.friendDraws)
        store.putInt(KEY_COMPUTER_PLAYER, scores.computerPlayer)
        store.putInt(KEY_COMPUTER_AI, scores.computerAi)
        store.putInt(KEY_COMPUTER_DRAWS, scores.computerDraws)
    }

    private companion object {
        const val KEY_FRIEND_X = "friend_x"
        const val KEY_FRIEND_O = "friend_o"
        const val KEY_FRIEND_DRAWS = "friend_draws"
        const val KEY_COMPUTER_PLAYER = "computer_player"
        const val KEY_COMPUTER_AI = "computer_ai"
        const val KEY_COMPUTER_DRAWS = "computer_draws"
    }
}
