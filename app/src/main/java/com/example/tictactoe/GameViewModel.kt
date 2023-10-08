package com.example.tictactoe

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class GameViewModel : ViewModel() {
    var state by mutableStateOf(GameState())
    var isPlayingWithFriend by mutableStateOf(true)


    val boardItems: MutableMap<Int, BoardCellValue> = mutableMapOf(
        1 to BoardCellValue.NONE,
        2 to BoardCellValue.NONE,
        3 to BoardCellValue.NONE,
        4 to BoardCellValue.NONE,
        5 to BoardCellValue.NONE,
        6 to BoardCellValue.NONE,
        7 to BoardCellValue.NONE,
        8 to BoardCellValue.NONE,
        9 to BoardCellValue.NONE,
    )

    fun onAction(action: UserAction) {
        when (action) {
            is UserAction.BoardTapped -> {
                addValueToBoard(action.cellNo)
                if (isComputerTurn()) {
                    viewModelScope.launch {
                        delay(500)
                        computerPlayer()
                    }
                }
            }

            UserAction.PlayAgainButtonClicked -> {
                if (state.hasWon || hasBoardFull()) {
                    gameReset()
                    if (isComputerTurn())
                        viewModelScope.launch {
                            delay(500)
                            computerPlayer()
                        }
                }
            }

            UserAction.SwitchPlayMode -> {
                isPlayingWithFriend = !isPlayingWithFriend
                gameReset()
                clearScore()

                if (isComputerTurn())
                    viewModelScope.launch {
                    delay(500)
                    computerPlayer()
                }
            }
        }
    }

    private fun gameReset() {
        val hintText = when {
            state.currentTurn == BoardCellValue.CIRCLE -> "Player 'O' turn"
            state.currentTurn == BoardCellValue.CROSS && isPlayingWithFriend -> "Player 'X' turn"
            else -> "Computer 'X' turn"
        }

        boardItems.forEach { (i, _) ->
            boardItems[i] = BoardCellValue.NONE
        }

        state = state.copy(
            hintText = hintText,
            victoryType = VictoryType.NONE,
            hasWon = false
        )
    }

    private fun addValueToBoard(cellNo: Int) {
        if (state.currentTurn == BoardCellValue.CIRCLE && !state.hasWon && isValidMove((cellNo)) && !isComputerTurn()) {
            boardItems[cellNo] = BoardCellValue.CIRCLE
            state = if (checkForVictory(BoardCellValue.CIRCLE)) {
                state.copy(
                    hintText = "Player 'O' Won",
                    playerCircleCount = state.playerCircleCount + 1,
                    currentTurn = BoardCellValue.CROSS,
                    hasWon = true
                )
            } else if (hasBoardFull()) {
                state.copy(
                    hintText = "Game Draw",
                    currentTurn = BoardCellValue.CROSS,
                    drawCount = state.drawCount + 1
                )
            } else {
                state.copy(
                    hintText = if (isPlayingWithFriend) "Player 'X' turn" else "Computer 'X' turn",
                    currentTurn = BoardCellValue.CROSS
                )
            }

        } else if (state.currentTurn == BoardCellValue.CROSS && !state.hasWon && isValidMove(cellNo)) {
            boardItems[cellNo] = BoardCellValue.CROSS
            state = if (checkForVictory(BoardCellValue.CROSS)) {
                state.copy(
                    hintText = if (isPlayingWithFriend) "Player 'X' Won" else "Computer 'X' Won",
                    playerCrossCount = state.playerCrossCount + 1,
                    currentTurn = BoardCellValue.CIRCLE,
                    hasWon = true
                )
            } else if (hasBoardFull()) {
                state.copy(
                    hintText = "Game Draw",
                    currentTurn = BoardCellValue.CIRCLE,
                    drawCount = state.drawCount + 1
                )
            } else {
                state.copy(
                    hintText = "Player 'O' turn",
                    currentTurn = BoardCellValue.CIRCLE
                )
            }
        }
    }
    private fun computerPlayer() {
        if (!state.hasWon) {
            val winningMove = findWinMove(BoardCellValue.CROSS)
            if (winningMove != -1) {
                addValueToBoard(winningMove)
                return
            }

            val blockingMove = findWinMove(BoardCellValue.CIRCLE)
            if (blockingMove != -1) {
                addValueToBoard(blockingMove)
                return
            }

            if (boardItems[5] == BoardCellValue.NONE) {
                addValueToBoard(5)
                return
            }

            val emptyCells = boardItems.filter { it.value == BoardCellValue.NONE }.keys.toList()
            if (emptyCells.isNotEmpty()) {
                val randomCell = emptyCells.random()
                addValueToBoard(randomCell)
            }
        }
    }

    private fun findWinMove(cellValue: BoardCellValue): Int {
        for (i in 1..9) {
            if (boardItems[i] == BoardCellValue.NONE) {
                boardItems[i] = cellValue
                if (checkForVictory(cellValue)) {
                    boardItems[i] = BoardCellValue.NONE
                    return i
                }
                boardItems[i] = BoardCellValue.NONE
            }
        }
        return -1
    }

    private fun checkForVictory(boardValue: BoardCellValue): Boolean {
        when {
            boardItems[1] == boardValue && boardItems[2] == boardValue && boardItems[3] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL1)
                return true
            }

            boardItems[4] == boardValue && boardItems[5] == boardValue && boardItems[6] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL2)
                return true
            }

            boardItems[7] == boardValue && boardItems[8] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL3)
                return true
            }

            boardItems[1] == boardValue && boardItems[4] == boardValue && boardItems[7] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL1)
                return true
            }

            boardItems[2] == boardValue && boardItems[5] == boardValue && boardItems[8] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL2)
                return true
            }

            boardItems[3] == boardValue && boardItems[6] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL3)
                return true
            }

            boardItems[1] == boardValue && boardItems[5] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.DIAGONAL1)
                return true
            }

            boardItems[3] == boardValue && boardItems[5] == boardValue && boardItems[7] == boardValue -> {
                state = state.copy(victoryType = VictoryType.DIAGONAL2)
                return true
            }

            else -> return false
        }
    }
    private fun clearScore() {
        state = state.copy(
            playerCircleCount = 0,
            playerCrossCount = 0,
            drawCount = 0
        )
    }

    private fun isComputerTurn(): Boolean {
        return !state.hasWon && !isPlayingWithFriend && state.currentTurn == BoardCellValue.CROSS
    }
    private fun isValidMove(cellNo : Int): Boolean {
        return boardItems[cellNo] == BoardCellValue.NONE
    }
    private fun hasBoardFull(): Boolean {
        return !boardItems.containsValue(BoardCellValue.NONE)
    }
}