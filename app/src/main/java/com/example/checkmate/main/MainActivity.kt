package com.example.checkmate.main

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import com.example.checkmate.ui.login.LoginActivity
import java.util.*
import android.net.Uri
import android.os.Environment
import android.util.Base64
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import android.graphics.BitmapFactory
import com.example.checkmate.itemSelection.ItemSelectionActivity
import java.nio.ByteBuffer


class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val PICK_BILL_PHOTO = 2;
    private lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.checkmate.R.layout.activity_main)
        scanButton.setOnClickListener { this.dispatchTakePictureIntent() }

        mainActivityViewModel = ViewModelProviders.of(this, MainActivityViewModelFactory(this.applicationContext))
            .get(MainActivityViewModel::class.java)

        if (!mainActivityViewModel.isLoggedIn) {
            val myIntent = Intent(this, LoginActivity::class.java)
            this.startActivity(myIntent)
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            val imageBitmap = data!!.extras.get("data") as Bitmap
//            imageView.setImageBitmap(imageBitmap)
            val photoPath = mainActivityViewModel.currentPhotoPath
            val bitmap = BitmapFactory.decodeFile(photoPath)

            val byteBuffer = ByteBuffer.allocate(bitmap.byteCount);
            bitmap.copyPixelsToBuffer(byteBuffer);
            val bytes = byteBuffer.toString().toByteArray();

            val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)

            val intent = Intent(this, ItemSelectionActivity::class.java)

            intent.putExtra("pathToPhoto", mainActivityViewModel.currentPhotoPath)
            startActivity(intent)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_BILL_PHOTO)
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
