package com.example.checkmate.joinBillSession

import android.app.Activity
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord.createMime
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Bundle
import android.widget.Toast
import com.example.checkmate.R

class JoinBillSessionActivity : Activity(), NfcAdapter.CreateNdefMessageCallback {

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_bill_session)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        // Register callback
        nfcAdapter?.setNdefPushMessageCallback(this, this)
    }

    override fun createNdefMessage(event: NfcEvent): NdefMessage {
        val text = "Beam me up, Android!\n\n" +
                "Beam Time: " + System.currentTimeMillis()
        return NdefMessage(
            arrayOf(
                createMime("application/vnd.com.example.android.beam", text.toByteArray())
            )
            /**
             * The Android Application Record (AAR) is commented out. When a device
             * receives a push with an AAR in it, the application specified in the AAR
             * is guaranteed to run. The AAR overrides the tag dispatch system.
             * You can add it back in to guarantee that this
             * activity starts when receiving a beamed message. For now, this code
             * uses the tag dispatch system.
             *///,NdefRecord.createApplicationRecord("com.example.android.beam")
        )
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
        val context = this;
        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMsgs ->
            (rawMsgs[0] as NdefMessage).apply {
                // record 0 contains the MIME type, record 1 is the AAR, if present
//                val toast = Toast(applicationContext)
//                toast.tex(String(records[0].payload))
//                toast.show()

                Toast.makeText(context, String(records[0].payload), Toast.LENGTH_LONG).show()
            }
        }
    }
}
