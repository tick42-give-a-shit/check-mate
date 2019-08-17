package com.example.checkmate.pay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.checkmate.R
import com.braintreepayments.cardform.view.CardForm


class CardFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_form)
        setupCardForm()
    }

    private fun setupCardForm() {
        val cardForm = findViewById(com.example.checkmate.R.id.card_form) as CardForm
        cardForm.cardRequired(true)
            .expirationRequired(true)
            .cvvRequired(true)
            .cardholderName(CardForm.FIELD_REQUIRED)
            .postalCodeRequired(true)
//            .mobileNumberRequired(true)
//            .mobileNumberExplanation("SMS is required on this number")
            .actionLabel("Pay")
            .setup(this)

        cardForm.setOnCardFormSubmitListener { onFormSubmitted(cardForm) }
    }

    private fun onFormSubmitted(cardForm: CardForm) {
        cardForm.cardNumber
        cardForm.expirationMonth
        cardForm.expirationYear
        cardForm.cvv
        cardForm.cardholderName
        cardForm.postalCode
        cardForm.countryCode
        cardForm.mobileNumber
    }
}
