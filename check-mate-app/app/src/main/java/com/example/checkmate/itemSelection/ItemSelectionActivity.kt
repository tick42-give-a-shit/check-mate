package com.example.checkmate.itemSelection

import android.graphics.Color
import java.nio.file.Paths
import java.nio.file.Files
import java.io.InputStreamReader
import java.io.BufferedReader
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import com.example.checkmate.misc.CustomView
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.example.checkmate.R
import kotlinx.android.synthetic.main.activity_item_selection.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.widget.Toast
import java.nio.charset.StandardCharsets
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.example.checkmate.data.JoinData

class ItemSelectionActivity : AppCompatActivity(), NfcAdapter.CreateNdefMessageCallback, View.OnTouchListener, View.OnLongClickListener {

    fun readFromFile(path: String): String {

        return String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private var nfcAdapter: NfcAdapter? = null
    private var joinData: JoinData? = null;
    private var longTouch: Boolean = false;

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_selection)

        var jsonPath = intent.getStringExtra("jsonPath")
        val json = readFromFile(jsonPath)
        var mapper = jacksonObjectMapper()
        joinData = mapper.readValue(json)
        var base64 = joinData!!.base64
        var decodedBytes = Base64.decode(base64, 0)
        var bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        //println(">>> FOO " + joinData!!.items.size)
        billPhoto.setJoinData(joinData)
        billPhoto.setImageBitmap(bitmap)
        billPhoto.setOnTouchListener(this)
        billPhoto.setOnLongClickListener(this)
        var r = Integer.valueOf( joinData!!.color!!.substring( 1, 3 ), 16 );
        var g = Integer.valueOf( joinData!!.color!!.substring( 3, 5 ), 16 );
        var b = Integer.valueOf( joinData!!.color!!.substring( 5, 7 ), 16 );

        simpleTextView.setBackgroundColor(Color.argb(60, r, g, b));

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