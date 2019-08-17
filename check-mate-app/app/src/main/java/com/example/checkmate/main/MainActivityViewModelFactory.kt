package com.example.checkmate.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.checkmate.api.Webservice
import com.example.checkmate.data.LoginDataSource
import com.example.checkmate.data.LoginRepository
import com.example.checkmate.data.MainActivityRepository

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class MainActivityViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(
                loginRepository = LoginRepository(
                    dataSource = LoginDataSource()
                ),
                mainActivityRepository = MainActivityRepository(Webservice.create(context))
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
