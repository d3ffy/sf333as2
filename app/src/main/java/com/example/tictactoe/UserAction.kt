package com.example.tictactoe

sealed class UserAction {
    object PlayAgainButtonClicked: UserAction()
    data class BoardTapped(val cellNo: Int): UserAction()
    object SwitchPlayMode: UserAction()
}
