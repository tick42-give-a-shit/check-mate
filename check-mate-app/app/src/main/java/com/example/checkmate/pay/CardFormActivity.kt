package com.example.checkmate.pay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.example.checkmate.R
import com.braintreepayments.cardform.view.CardForm
import com.example.checkmate.data.SessionState
import com.example.checkmate.finalPayment.FinalPaymentActivity
import kotlinx.android.synthetic.main.activity_card_form.*


class CardFormActivity : AppCompatActivity() {

    lateinit var sessionState: SessionState
    lateinit var viewModel: CardFormViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_form)
        setupCardForm()

        viewModel = ViewModelProviders.of(this, CardFormViewModelFactory(this.applicationContext))
            .get(CardFormViewModel::class.java)

        sessionState = intent.getParcelableExtra("sessionState")

        payButton.setOnClickListener { onFormSubmitted(card_form as CardForm) }
    }

    private fun setupCardForm() {
        val cardForm = card_form as CardForm
        cardForm.cardRequired(true)
            .expirationRequired(true)
            .cvvRequired(true)
            .cardholderName(CardForm.FIELD_REQUIRED)
            .postalCodeRequired(true)
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

        if (sessionState.billId == null || sessionState.myColor == null) {
            Toast.makeText(this, "Something went wrong please start again", Toast.LENGTH_LONG).show()
// Testing
            // return
        } else {
            viewModel.payBill(sessionState.billId!!, sessionState.myColor!!)

        }


        if (sessionState.isCreator) {
            val intent = Intent(this, FinalPaymentActivity::class.java)

            startActivity(intent)
        }
    }
}
