package com.example.checkmate.pay

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.checkmate.api.Webservice
import com.example.checkmate.data.LoginDataSource
import com.example.checkmate.data.LoginRepository
import com.example.checkmate.data.MainActivityRepository
import com.example.checkmate.main.MainActivityViewModel

class CardFormViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardFormViewModel::class.java)) {
            return CardFormViewModel(
                cardFormRepository = CardFormRepository(Webservice.create(context))
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}