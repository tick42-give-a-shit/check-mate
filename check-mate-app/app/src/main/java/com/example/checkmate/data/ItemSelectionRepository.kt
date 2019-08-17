package com.example.checkmate.data

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.example.checkmate.api.Webservice
import com.example.checkmate.data.model.BillDetailsResponse
import com.example.checkmate.data.model.SelectedItem
import com.example.checkmate.data.model.SelectItemRequest
import com.squareup.moshi.Moshi
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItemSelectionRepository(val api: Webservice) {

    val itemDetailsResponse: MutableLiveData<BillDetailsResponse> = MutableLiveData()

    lateinit var timer: Timer

    fun pollItemDetails(billId: String) {
        timer = Timer()

        timer.scheduleAtFixedRate(
            object : TimerTask() {

                override fun run() {

                    val result = api.getBillDetails(billId!!.toLong()).execute()

                    itemDetailsResponse.postValue(result.body())
                }

            },
            //Set how long before to start calling the TimerTask (in milliseconds)
            0,
            //Set the amount of time between each execution (in milliseconds)
            5000
        )
    }

    fun cancelPolling() {
        timer.cancel()
    }

    fun selectItem(item: SelectItemRequest) {
        api.selectItem(item).enqueue(object : Callback<Unit> {
            override fun onFailure(call: Call<Unit>, t: Throwable) {
            }

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
            }
        })
    }
}