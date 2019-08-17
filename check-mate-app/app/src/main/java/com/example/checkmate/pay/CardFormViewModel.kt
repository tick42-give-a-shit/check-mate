package com.example.checkmate.pay

import androidx.lifecycle.ViewModel

class CardFormViewModel(val cardFormRepository: CardFormRepository) : ViewModel() {

    fun payBill(billId: Long, colour: String) {
        cardFormRepository.payMyBill(billId, colour)
    }

}