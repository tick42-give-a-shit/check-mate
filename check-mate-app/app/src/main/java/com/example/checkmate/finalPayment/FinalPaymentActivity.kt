package com.example.checkmate.finalPayment


import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.example.checkmate.R
import com.example.checkmate.data.model.ItemStatus
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.activity_final_payment.*
import android.graphics.*
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.checkmate.data.SessionState
import com.example.checkmate.data.model.ItemPaymentDetails
import com.example.checkmate.itemSelection.ItemSelectionViewModel
import com.example.checkmate.itemSelection.ItemSelectionViewModelFactory
import com.example.checkmate.main.MainActivity

class ColorToPaid(val color: String, val hasPaid: Boolean)

class FinalPaymentAdapter(val context: Context, private val mData: List<ColorToPaid>) :
    RecyclerView.Adapter<FinalPaymentAdapter.ViewHolder>() {
    private lateinit var mInflater: LayoutInflater
    private var mClickListener: ItemClickListener? = null


    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        this.mInflater = LayoutInflater.from(parent.context)

        val view = mInflater.inflate(R.layout.final_payment_item, parent, false)


        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData[position]
        holder.myIconView.text = item.color + " " + item.hasPaid.toString()
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mData.size
    }


    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        internal var myIconView: TextView


        init {
            myIconView = itemView.findViewById(R.id.resultText)

            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
        }
    }

    // convenience method for getting data at click position
    internal fun getItem(id: Int): ColorToPaid {
        return mData[id]
    }

    // allows clicks events to be caught
    internal fun setClickListener(itemClickListener: ItemClickListener) {
        this.mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}

class FinalPaymentActivity : AppCompatActivity() {

    private lateinit var viewModel: ItemSelectionViewModel
    private lateinit var listOfData: ArrayList<ColorToPaid>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_payment)

        val sessionState = intent.getParcelableExtra<SessionState>("sessionState")
        viewModel = ViewModelProviders.of(this, ItemSelectionViewModelFactory(this))
            .get(ItemSelectionViewModel::class.java)



        listOfData = arrayListOf()

        val myAdapter: FinalPaymentAdapter = FinalPaymentAdapter(this, listOfData.toList())
        listLayout.adapter = myAdapter;
        listLayout.layoutManager = LinearLayoutManager(this)

        viewModel.poll(sessionState.billId.toString())
        myAdapter.notifyDataSetChanged()

        poll()

        finalPayButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)

            startActivity(intent)
        }


    }

    private fun poll() {
        viewModel.itemDetails.observe(this, Observer { t ->


            val colors = getColors(t.items.toList())
//
            val newList = colors.map { c -> ColorToPaid(c, hasColorPaid(c, t.items.toList())) }

            listOfData.clear()
            listOfData.addAll(newList)
            listLayout.adapter!!.notifyDataSetChanged()


        })

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
            //card.addView(imageView)
        }


        val bg = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bg)
        val paint = Paint()
        var r = Integer.valueOf(color!!.substring(1, 3), 16);
        var g = Integer.valueOf(color!!.substring(3, 5), 16);
        var b = Integer.valueOf(color!!.substring(5, 7), 16);
        paint.color = Color.argb(100, r, g, b);
        canvas.drawRect(Rect(50, 10, 200, 200), paint)

        val imageViewForDrawable = ImageView(this)

        imageViewForDrawable.setImageBitmap(bg)
        card.addView(imageViewForDrawable)



        return card
    }
}
