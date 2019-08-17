package com.example.checkmate.itemSelection

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import com.example.checkmate.R
import kotlinx.android.synthetic.main.activity_item_selection.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.widget.Toast
import com.example.checkmate.pay.CardFormActivity


class ItemSelectionActivity : AppCompatActivity(), NfcAdapter.CreateNdefMessageCallback {

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_selection)

        val pathPhoto = intent.getStringExtra("pathToPhoto")
        if (pathPhoto != null) {
            val myBitmap = BitmapFactory.decodeFile(pathPhoto)
            billPhoto.setImageBitmap(myBitmap)
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show()
            //finish()
            //return
        }
        // Register callback
        nfcAdapter?.setNdefPushMessageCallback(this, this)

        continueButton.setOnClickListener {
            val intent = Intent(this, CardFormActivity::class.java)
            intent.putExtra("amount", 333)
            startActivity(intent)
        }
    }

    override fun createNdefMessage(event: NfcEvent): NdefMessage {
        val text = "Beam me up, Android!\n\n" +
                "Beam Time: " + System.currentTimeMillis()
        return NdefMessage(
            arrayOf(
                NdefRecord.createMime("application/vnd.com.example.android.beam", text.toByteArray())
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

}
