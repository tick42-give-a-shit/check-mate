package com.example.checkmate.finalPayment


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.example.checkmate.R
import com.example.checkmate.data.model.ItemStatus
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.activity_final_payment.*
import android.graphics.*


class FinalPaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_payment)


        //linearLayout.addView()
    }

    private fun getColors(billDetails: ArrayList<ItemStatus>): List<String> {
        return billDetails.flatMap { i -> i.details }.map { ipd -> ipd.color }.distinct()
    }

    private fun hasColorPaid(color: String, billDetails: ArrayList<ItemStatus>): Boolean {
        return billDetails.flatMap { i -> i.details }.filter { ipd -> ipd.color == color }
            .all { ipd -> ipd.hasBeenPaidFor }
    }

    private fun createViewForColor(color: String, hasPaidFor: Boolean): View {
        val card = MaterialCardView(this)


        val imageView = ImageView(this)
        if (hasPaidFor) {
            var drawable = this.resources.getDrawable(R.drawable.abc_btn_check_material)

            if (android.os.Build.VERSION.SDK_INT > 21) {
                drawable = this.resources.getDrawable(R.drawable.abc_btn_check_material, theme)
            }

            imageView.setImageDrawable(drawable)
            card.addView(imageView)
        } else {
            var drawable = this.resources.getDrawable(R.drawable.abc_list_divider_mtrl_alpha)

            if (android.os.Build.VERSION.SDK_INT > 21) {
                drawable = this.resources.getDrawable(R.drawable.abc_btn_check_material, theme)
            }

            imageView.setImageDrawable(drawable)
            card.addView(imageView)
        }


        val bg = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bg)
        val paint = Paint()
        paint.color = Color.parseColor(color)
        canvas.drawRect(Rect(50, 80, 200, 200), paint)

        val imageViewForDrawable = ImageView(this)

        imageViewForDrawable.setImageBitmap(bg)
        card.addView(imageViewForDrawable)

        return card
    }
}
