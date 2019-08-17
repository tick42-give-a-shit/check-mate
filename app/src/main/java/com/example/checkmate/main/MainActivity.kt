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
import android.app.Activity
import android.util.Base64



class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val PICK_BILL_PHOTO = 2;
    private lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.checkmate.R.layout.activity_main)
        scanButton.setOnClickListener { this.pickImage() }

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
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data!!.extras.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
            //mainActivityViewModel.sendBillPhoto(Base64.getEncoder().)
        }
        if (requestCode == PICK_BILL_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return
            }
            val inputStream = this.applicationContext.getContentResolver().openInputStream(data.data)
            val picture = inputStream.readBytes()
            val base64 = Base64.encodeToString(picture, Base64.DEFAULT)
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_BILL_PHOTO)
    }

}
