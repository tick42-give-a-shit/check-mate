package com.example.checkmate.itemSelection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.checkmate.data.ItemSelectionRepository
import com.example.checkmate.data.model.BillDetailsResponse
import com.example.checkmate.data.model.SelectedItem

class ItemSelectionViewModel(val repository: ItemSelectionRepository) : ViewModel() {

    var itemDetails: MutableLiveData<BillDetailsResponse> = MutableLiveData()

    fun poll(billId: String) {
        repository.pollItemDetails(billId)
        itemDetails = repository.itemDetailsResponse
    }

    override fun onCleared() {
        repository.cancelPolling()
        super.onCleared()
    }

    fun selectItem(name: String, myColor: String) {
        val item = SelectedItem(name, myColor)
        repository.selectItem(item)
    }
}