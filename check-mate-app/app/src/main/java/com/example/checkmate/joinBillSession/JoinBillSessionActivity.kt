package com.example.checkmate.joinBillSession

import android.app.Activity
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord.createMime
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.checkmate.R
import com.example.checkmate.data.DataHolder
import com.example.checkmate.data.JoinDataItem
import com.example.checkmate.data.Rect
import com.example.checkmate.data.SessionState
import com.example.checkmate.itemSelection.ItemSelectionActivity
import com.example.checkmate.main.MainActivityViewModel
import com.example.checkmate.main.MainActivityViewModelFactory

class JoinBillSessionActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null

    lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_bill_session)

        mainActivityViewModel = ViewModelProviders.of(this, MainActivityViewModelFactory(this.applicationContext))
            .get(MainActivityViewModel::class.java)

    }

    private fun subscribeForBillSession() {
        mainActivityViewModel.currentBillSession.observe(this, Observer { billSession ->

            mainActivityViewModel.currentBillSession.removeObservers(this)

            val sessionState = SessionState(null, false, 0.0, 0.0, null, null)
            sessionState.billId = billSession.id
            sessionState.myColor = billSession.color
            sessionState.restaurant = billSession.restaurant

            val intent = Intent(this, ItemSelectionActivity::class.java)
            intent.putExtra("sessionState", sessionState)
            if (mainActivityViewModel.currentPhotoPath != null) {
                intent.putExtra("pathToPhoto", mainActivityViewModel.currentPhotoPath)

            }
            DataHolder.base64 = billSession.base64
            DataHolder.items = billSession.items.map { i ->
                val rectangle = Rect(
                    i.position.x.toFloat(),
                    i.position.y.toFloat(),
                    i.position.w.toFloat(),
                    i.position.h.toFloat()
                )
                JoinDataItem(
                    rectangle,
                    i.name,
                    i.quantity.toFloat(),
                    i.quantity.toFloat(),
                    i.unitPrice.toFloat(),
                    false
                )
            }.toTypedArray()

            val totalRectangle = Rect(
                billSession.total.position.x.toFloat(),
                billSession.total.position.y.toFloat(),
                billSession.total.position.w.toFloat(),
                billSession.total.position.h.toFloat()
            )

            DataHolder.total = JoinDataItem(totalRectangle, null, null, billSession.total.amount.toFloat(), null, null)
            //intent.putExtra("base64", billSession.base64)

            startActivity(intent)
        })
    }

    override fun onResume() {
        super.onResume()
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            processIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        //textView = findViewById(R.id.textView)
        // only one message sent during the beam
        val context = this
        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMsgs ->
            (rawMsgs[0] as NdefMessage).apply {
                // record 0 contains the MIME type, record 1 is the AAR, if present
//                val toast = Toast(applicationContext)
//                toast.tex(String(records[0].payload))
//                toast.show()
                val billIdAsString = String(records[0].payload)
                subscribeForBillSession()
                mainActivityViewModel.joinPaymentSession(billIdAsString)
                //Toast.makeText(context, String(records[0].payload), Toast.LENGTH_LONG).show()
            }
        }
    }
}
