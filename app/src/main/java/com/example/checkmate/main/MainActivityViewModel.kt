package com.example.checkmate.main

import androidx.lifecycle.ViewModel
import com.example.checkmate.data.LoginRepository
import com.example.checkmate.data.MainActivityRepository

class MainActivityViewModel(
    private val loginRepository: LoginRepository,
    private val mainActivityRepository: MainActivityRepository
) : ViewModel() {

    val loggedInUser = loginRepository.user
    val isLoggedIn = loginRepository.isLoggedIn

    fun sendBillPhoto(data: String) {
        this.mainActivityRepository.sendBillPhoto(data)
    }

}
