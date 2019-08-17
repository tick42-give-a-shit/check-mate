package com.example.checkmate.main

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.checkmate.data.model.BillSession
import com.example.checkmate.data.SessionState
import com.example.checkmate.itemSelection.ItemSelectionActivity
import com.example.checkmate.joinBillSession.JoinBillSessionActivity
import com.example.checkmate.R
import com.example.checkmate.data.DataHolder
import com.example.checkmate.data.JoinDataItem
import com.example.checkmate.data.Rect
import com.example.checkmate.ui.login.LoginActivity
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private val PICK_BILL_PHOTO = 2
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var sessionState: SessionState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.checkmate.R.layout.activity_main)
        scanButton.setOnClickListener { onScanClicked() }
        joinButton.setOnClickListener { this.joinBillSession() }

        mainActivityViewModel = ViewModelProviders.of(this, MainActivityViewModelFactory(this.applicationContext))
            .get(MainActivityViewModel::class.java)

        sessionState = SessionState(null, false, 0.0, 0.0, null, null)

        if (!mainActivityViewModel.isLoggedIn) {
            val myIntent = Intent(this, LoginActivity::class.java)
            this.startActivity(myIntent)
        }
    }


    private fun onScanClicked() {
        AlertDialog.Builder(this)
            .setNeutralButton("Pick a photo") { dialog, _ ->
                pickImage()
            }.setPositiveButton("Take a photo") { _, _ ->
                dispatchTakePictureIntent()
            }.setCancelable(true).show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = createImageFile()
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(this, "com.example.checkmate.fileprovider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun joinBillSession() {
        val intent = Intent(this, JoinBillSessionActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var base64: String? = null
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val photoPath = mainActivityViewModel.currentPhotoPath
            var bitmap = BitmapFactory.decodeFile(photoPath)
            // bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bill)

            var byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream)
            var byteArray = byteArrayOutputStream.toByteArray()
            byteArrayOutputStream.close()

            base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            subscribeForBillSession()
            sessionState.isCreator = true

            mainActivityViewModel.sendBillPhoto(base64)

        }

        if (requestCode == PICK_BILL_PHOTO) {
            if (data == null) {
                //Display an error
                return
            }
            val inputStream = this.applicationContext.getContentResolver().openInputStream(data.data)
            val picture = inputStream.readBytes()
            base64 = Base64.encodeToString(picture, Base64.NO_WRAP)

            subscribeForBillSession()
            sessionState.isCreator = true


            mainActivityViewModel.sendBillPhoto(base64)
        }

        // subscribeForBillSession()

        sessionState.isCreator = true

        val intent = Intent(this, ItemSelectionActivity::class.java)
        intent.putExtra("sessionState", sessionState)

        if (mainActivityViewModel.currentPhotoPath != null) {

//            PostTask().execute(
//                base64 as Object,
//                intent as Object,
//                this as Object,
//                (mainActivityViewModel.currentPhotoPath + ".json") as Object
//            )

        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun subscribeForBillSession() {
        mainActivityViewModel.currentBillSession.observe(this, Observer { billSession ->

            mainActivityViewModel.currentBillSession.removeObservers(this)

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
                    null
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

    private fun pickImage() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"

        val pickIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(getIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

        startActivityForResult(chooserIntent, PICK_BILL_PHOTO)
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

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mainActivityViewModel.currentPhotoPath = absolutePath
        }
    }
}

//internal class PostTask : AsyncTask<Object, Void, Object>() {
//
//    override fun doInBackground(vararg params: Object): Object? {
//
//        var base64: String = params[0] as String
//        var intent: Intent = params[1] as Intent
//        var activity: MainActivity = params[2] as MainActivity
//        var jsonPath: String = params[3] as String
//        println(">>> PostTask.doInBackground HERE 1 ")
//        var stuff = "{}"
//        //stuff = ("{ \"base64\": \"" + base64 + "\"}")
//
//        var sb = StringBuilder()
//        try {
//            //val url = URL("http://192.168.0.100:17723/new")
//            val url = URL("https://give-a-shit-check-mate.herokuapp.com/new")
//            //val url = URL("http://149.62.203.36/new")
//
//            with(url.openConnection() as HttpURLConnection) {
//
//                requestMethod = "POST"
//
//                var data = stuff.toByteArray(StandardCharsets.UTF_8)
//
//                var wr = DataOutputStream(getOutputStream())
//                wr.use {
//                    wr.write(data)
//                }
//
//                //inputStream.bufferedReader().use {
//                //    it.lines().forEach { line ->
//                //        println(line)
//                //    }
//                //}
//                writeToFile(jsonPath, inputStream, activity.applicationContext)
//                println(jsonPath)
//                println(">>> PostTask.doInBackground HERE 2 ")
//                intent.putExtra("jsonPath", jsonPath)
//                activity.startActivity(intent)
//            }
//
//            return null
//
//        } catch (e: Exception) {
//
//            println(">>> PostTask.doInBackground " + e)
//            println(e.printStackTrace())
//            //handle exception
//            return null
//        }
//    }
//
//    fun readFully(inputStream: InputStream): ByteArrayOutputStream {
//        var baos = ByteArrayOutputStream()
//        var buffer = ByteArray(1024)
//        var length = 0
//        while (true) {
//            length = inputStream.read(buffer)
//            if (length == -1)
//                break
//            baos.write(buffer, 0, length)
//        }
//        return baos
//    }
//
//    fun writeToFile(path: String, data: InputStream, context: Context) {
//        var buffer = readFully(data).toByteArray()
//        File(path).writeBytes(buffer)
//    }
//}