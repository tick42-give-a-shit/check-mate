package com.example.checkmate.itemSelection

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.checkmate.api.Webservice
import com.example.checkmate.data.ItemSelectionRepository
import com.example.checkmate.data.LoginDataSource
import com.example.checkmate.data.LoginRepository
import com.example.checkmate.data.MainActivityRepository
import com.example.checkmate.main.MainActivityViewModel

class ItemSelectionViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemSelectionViewModel::class.java)) {
            val api = Webservice.create(context)
            return ItemSelectionViewModel(repository = ItemSelectionRepository(api)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}