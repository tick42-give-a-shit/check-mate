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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.checkmate.data.SessionState
import com.example.checkmate.data.model.ItemPaymentDetails
import com.example.checkmate.itemSelection.ItemSelectionViewModel
import com.example.checkmate.itemSelection.ItemSelectionViewModelFactory


class FinalPaymentActivity : AppCompatActivity() {

    private lateinit var viewModel: ItemSelectionViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_payment)

        val sessionState = intent.getParcelableExtra<SessionState>("sessionState")
        viewModel = ViewModelProviders.of(this, ItemSelectionViewModelFactory(this.applicationContext))
            .get(ItemSelectionViewModel::class.java)
        viewModel.poll(sessionState.billId.toString())
        poll()

    }

    private fun poll() {
        viewModel.itemDetails.observe(this, Observer { t ->
            val colors = getColors(t.items.toList())

            colors.forEach { c ->
                val view = createViewForColor(c, hasColorPaid(c, t.items.toList()))

                linearLayout.addView(view)
            }

            val itemsList = arrayListOf<ItemStatus>(ItemStatus("lasagna", arrayOf(ItemPaymentDetails("#FFFFFF",1,true))),ItemStatus("pizza",
                arrayOf(ItemPaymentDetails("#333333",1,false))))


        })


        val itemsList = arrayListOf<ItemStatus>(ItemStatus("lasagna", arrayOf(ItemPaymentDetails("#FFFFFF",1,true))),ItemStatus("pizza",
            arrayOf(ItemPaymentDetails("#333333",1,false))))

        val colors = getColors(itemsList)

        colors.forEach { c ->
            val view = createViewForColor(c, hasColorPaid(c,itemsList))

            linearLayout.addView(view)
        }

    }

    private fun getColors(billDetails: List<ItemStatus>): List<String> {
        return billDetails.flatMap { i -> i.details.toList() }.map { ipd -> ipd.color }.distinct()
    }

    private fun hasColorPaid(color: String, billDetails: List<ItemStatus>): Boolean {
        return billDetails.flatMap { i -> i.details.toList() }.filter { ipd -> ipd.color == color }
            .all { ipd -> ipd.hasBeenPaidFor }
    }

    private fun createViewForColor(color: String, hasPaidFor: Boolean): View {
        val card = MaterialCardView(this)


        val imageView = ImageView(this)
        if (hasPaidFor) {
            var drawable = this.resources.getDrawable(R.drawable.icons8_checkmark_24)

            if (android.os.Build.VERSION.SDK_INT > 21) {
                drawable = this.resources.getDrawable(R.drawable.icons8_checkmark_24, theme)
            }

            imageView.setImageDrawable(drawable)
            //card.addView(imageView)
        } else {
            var drawable = this.resources.getDrawable(R.drawable.icons8_unavailable_24)

            if (android.os.Build.VERSION.SDK_INT > 21) {
                drawable = this.resources.getDrawable(R.drawable.icons8_unavailable_24, theme)
            }

            imageView.setImageDrawable(drawable)
            card.addView(imageView)
        }


        val bg = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bg)
        val paint = Paint()
        paint.color = Color.parseColor(color)
        canvas.drawRect(Rect(50, 10, 200, 200), paint)

        val imageViewForDrawable = ImageView(this)

        imageViewForDrawable.setImageBitmap(bg)
        card.addView(imageViewForDrawable)



        return card
    }
}
