package com.example.checkmate.pay

import androidx.lifecycle.ViewModel

class CardFormViewModel(val cardFormRepository: CardFormRepository) : ViewModel() {

    fun payBill(billId: String, colour: String) {
        cardFormRepository.payMyBill(billId, colour)
    }

}