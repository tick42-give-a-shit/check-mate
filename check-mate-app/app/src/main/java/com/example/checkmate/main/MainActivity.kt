package com.example.checkmate.main

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import com.example.checkmate.ui.login.LoginActivity
import android.net.Uri
import android.os.Environment
import android.util.Base64
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.example.checkmate.data.SessionState
import com.example.checkmate.data.model.BillSession
import com.example.checkmate.itemSelection.ItemSelectionActivity
import com.example.checkmate.joinBillSession.JoinBillSessionActivity
import java.nio.ByteBuffer
import java.util.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private val PICK_BILL_PHOTO = 2;
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var sessionState: SessionState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.checkmate.R.layout.activity_main)
        scanButton.setOnClickListener { onScanClicked() }
        joinButton.setOnClickListener { this.joinBillSession() }

        mainActivityViewModel = ViewModelProviders.of(this, MainActivityViewModelFactory(this.applicationContext))
            .get(MainActivityViewModel::class.java)

        sessionState = SessionState(null, false, 0.0, 0.0, null)

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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val photoPath = mainActivityViewModel.currentPhotoPath
            val bitmap = BitmapFactory.decodeFile(photoPath)

            val byteBuffer = ByteBuffer.allocate(bitmap.byteCount)
            bitmap.copyPixelsToBuffer(byteBuffer)
            val bytes = byteBuffer.toString().toByteArray()

            subscribeForBillSession()

            val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
            mainActivityViewModel.sendBillPhoto(base64)
            sessionState.isCreator = true
        }

        if (requestCode == PICK_BILL_PHOTO) {
            if (data == null) {
                //Display an error
                return
            }
            val inputStream = this.applicationContext.getContentResolver().openInputStream(data.data)
            val picture = inputStream.readBytes()
            val base64 = Base64.encodeToString(picture, Base64.DEFAULT)

            subscribeForBillSession()

            mainActivityViewModel.sendBillPhoto(base64)
            sessionState.isCreator = true
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun subscribeForBillSession() {
        mainActivityViewModel.currentBillSession.observe(this, Observer { billSession ->

            mainActivityViewModel.currentBillSession.removeObservers(this)

            sessionState.billId = billSession.billId
            sessionState.isCreator = true
            sessionState.myColor = billSession.myColor

            val intent = Intent(this, ItemSelectionActivity::class.java)
            intent.putExtra("sessionState", sessionState)
            if (mainActivityViewModel.currentPhotoPath != null) {
                intent.putExtra("pathToPhoto", mainActivityViewModel.currentPhotoPath)

            }
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
