package com.example.checkmate.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.checkmate.data.LoginRepository
import com.example.checkmate.data.MainActivityRepository
import com.example.checkmate.data.model.BillSession

class MainActivityViewModel(
    private val loginRepository: LoginRepository,
    private val mainActivityRepository: MainActivityRepository
) : ViewModel() {

    val loggedInUser = loginRepository.user
    val isLoggedIn = loginRepository.isLoggedIn

    var currentPhotoPath: String? = null

    val currentBillSession: LiveData<BillSession>

    init {
        currentBillSession = mainActivityRepository.currentBillSession;
    }

    fun sendBillPhoto(data: String) {
        this.mainActivityRepository.sendBillPhoto(data)
    }

}
