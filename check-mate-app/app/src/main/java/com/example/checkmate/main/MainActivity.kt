package com.example.checkmate.main

import com.example.checkmate.R
import android.content.Context
import java.nio.file.Paths
import java.nio.file.Files
import android.os.AsyncTask
import java.net.HttpURLConnection
import java.io.DataOutputStream
import java.net.URL
import java.io.InputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
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
import com.example.checkmate.joinBillSession.JoinBillSessionActivity
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val PICK_BILL_PHOTO = 2
    private lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onResume()
    {
        super.onResume()
        val intent = Intent(this, ItemSelectionActivity::class.java)
        intent.putExtra("jsonPath", "/storage/emulated/0/Android/data/com.example.checkmate/files/Pictures/JPEG_20190817_045033_4484398122197675981.jpg-here")
        startActivity(intent)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.checkmate.R.layout.activity_main)
        scanButton.setOnClickListener { this.dispatchTakePictureIntent() }
        joinButton.setOnClickListener { this.joinBillSession() }

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

    private fun joinBillSession() {
        val intent = Intent(this, JoinBillSessionActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

//            val imageBitmap = data!!.extras.get("data") as Bitmap
//            imageView.setImageBitmap(imageBitmap)

            val photoPath = mainActivityViewModel.currentPhotoPath
            var bitmap = BitmapFactory.decodeFile(photoPath)
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bill)

            var byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            var byteArray = byteArrayOutputStream.toByteArray()
            byteArrayOutputStream.close()

            val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            val intent = Intent(this, ItemSelectionActivity::class.java)

            // Files.deleteIfExists(Paths.get(mainActivityViewModel.currentPhotoPath))

            PostTask().execute(
                base64 as Object,
                intent as Object,
                this as Object,
                (mainActivityViewModel.currentPhotoPath + "-here") as Object)
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

internal class PostTask : AsyncTask<Object, Void, Object>() {

    override fun doInBackground(vararg params: Object): Object? {

        var base64: String = params[0] as String
        var intent: Intent = params[1] as Intent
        var activity: MainActivity = params[2] as MainActivity
        var jsonPath: String = params[3] as String
        println(">>> PostTask.doInBackground HERE 1 ")
        var stuff = "{}" 
        //stuff = ("{ \"base64\": \"" + base64 + "\"}")
        
        var sb = StringBuilder()
        try {
           //val url = URL("http://192.168.0.100:17723/new")
           val url = URL("http://give-a-shit-check-mate.herokuapp.com/new")
           //val url = URL("http://149.62.203.36/new")

            with(url.openConnection() as HttpURLConnection) {

                requestMethod = "POST"

                var data = stuff.toByteArray(StandardCharsets.UTF_8)

                var wr = DataOutputStream(getOutputStream())
                wr.use {
                   wr.write(data)
                }

                //inputStream.bufferedReader().use {
                //    it.lines().forEach { line ->
                //        println(line)
                //    }
                //}
                writeToFile(jsonPath, inputStream, activity.applicationContext)
                println(jsonPath)
                println(">>> PostTask.doInBackground HERE 2 ")
                intent.putExtra("jsonPath", jsonPath)
                activity.startActivity(intent)
            }

            return null

         } catch (e: Exception) {

            println(">>> PostTask.doInBackground " + e)
            println(e.printStackTrace())
            //handle exception
            return null
        }
    }

    fun readFully(inputStream: InputStream): ByteArrayOutputStream {
        var baos = ByteArrayOutputStream()
        var buffer = ByteArray(1024)
        var length = 0
        while (true) {
            length = inputStream.read(buffer)
            if (length == -1)
                break
            baos.write(buffer, 0, length)
        }
        return baos
    }

    fun writeToFile(path: String, data: InputStream, context: Context) {
        var buffer = readFully(data).toByteArray()
        File(path).writeBytes(buffer)
    }
}