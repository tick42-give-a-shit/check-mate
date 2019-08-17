package com.example.checkmate.itemSelection

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import com.example.checkmate.R
import kotlinx.android.synthetic.main.activity_item_selection.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap


class ItemSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_selection)

        val pathPhoto = intent.getStringExtra("pathToPhoto")
        if (pathPhoto != null) {
            val myBitmap = BitmapFactory.decodeFile(pathPhoto)
            billPhoto.setImageBitmap(myBitmap)
        }
    }
}
