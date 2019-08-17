package com.example.checkmate.itemSelection

import java.text.DecimalFormat
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Bundle
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.checkmate.data.JoinData
import com.example.checkmate.data.model.BillSession
import com.example.checkmate.data.SessionState
import com.example.checkmate.misc.CustomView
import com.example.checkmate.pay.CardFormActivity
import com.example.checkmate.R
import com.example.checkmate.data.DataHolder
import com.example.checkmate.data.JoinDataItem
import com.example.checkmate.data.model.BillDetailsResponse
import com.example.checkmate.main.MainActivityViewModel
import com.example.checkmate.main.MainActivityViewModelFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.material.snackbar.Snackbar
import com.squareup.moshi.Moshi
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import kotlinx.android.synthetic.main.activity_item_selection.*

class ItemSelectionActivity : AppCompatActivity(), NfcAdapter.CreateNdefMessageCallback, View.OnTouchListener,
    View.OnLongClickListener {

//    fun readFromFile(path: String): String {
//
//        return String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
//    }

    private var nfcAdapter: NfcAdapter? = null
    private var joinData: JoinData? = null;
    private var longTouch: Boolean = false;
    private var total: Double = 0.0;

    private lateinit var viewModel: ItemSelectionViewModel

    override fun onLongClick(v: View): Boolean {
        longTouch = true;
        return false;
    }

    override fun onTouch(v: View, e: MotionEvent): Boolean {
        if (longTouch) {
            longTouch = false;
        }
        return false;
    }

    private lateinit var sessionState: SessionState

    fun addTotal(added: Double) {
        total += added
        var decimalFormat = DecimalFormat("0.00")
        doneButton.text = "Total: " + decimalFormat.format(total) + " BGN"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_selection)
        // var jsonPath = intent.getStringExtra("jsonPath")
        // val json = readFromFile(jsonPath)
        // var mapper = jacksonObjectMapper()
        // joinData = mapper.readValue(json)
        sessionState = intent.getParcelableExtra("sessionState")
        joinData = JoinData(
            sessionState.billId!!.toString(),
            DataHolder.base64!!,
            sessionState.restaurant!!,
            DataHolder.items!!,
            DataHolder.total!!,
            sessionState.myColor
        )
        viewModel = ViewModelProviders.of(this, ItemSelectionViewModelFactory(this.applicationContext))
            .get(ItemSelectionViewModel::class.java)
        var base64 = DataHolder.base64
        var decodedBytes = Base64.decode(base64, 0)
        var bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        //println(">>> FOO " + joinData!!.items.size)
        billPhoto.setJoinData(joinData)
        billPhoto.setImageBitmap(bitmap)
        billPhoto.setLifecycleOwner(this)
        billPhoto.setViewModel(viewModel)
        billPhoto.setOnTouchListener(this)
        billPhoto.setOnLongClickListener(this)
        var r = Integer.valueOf(joinData!!.color!!.substring(1, 3), 16);
        var g = Integer.valueOf(joinData!!.color!!.substring(3, 5), 16);
        var b = Integer.valueOf(joinData!!.color!!.substring(5, 7), 16);

        simpleTextView.setBackgroundColor(Color.argb(60, r, g, b))

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show()
            //finish()
            //return
        }
        // Register callback
        nfcAdapter?.setNdefPushMessageCallback(this, this)
        
        doneButton.text = "Total: 0.00 BGN"

        doneButton.setOnClickListener {
            val intent = Intent(this, CardFormActivity::class.java)
            sessionState.personalPaymentAmount = 333.0
            intent.putExtra("sessionState", sessionState)
            startActivity(intent)
        }

        viewModel.poll(sessionState.billId.toString())
        poll()

    }

    private fun poll() {
        viewModel.itemDetails.observe(this, Observer { t->
            billPhoto.onChanged(t)
        })

    }

    override fun createNdefMessage(event: NfcEvent): NdefMessage {
        val text = sessionState.billId.toString()
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