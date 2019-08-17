package com.example.checkmate.pay

import androidx.lifecycle.MutableLiveData
import com.example.checkmate.api.Webservice
import com.example.checkmate.data.model.BillSession
import com.example.checkmate.data.model.PayRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CardFormRepository(val api: Webservice) {

    val currentBillSession: MutableLiveData<BillSession> by lazy {
        MutableLiveData<BillSession>()
    }

    fun payMyBill(billId: Long, colour: String) {
        val payRequest = PayRequest(billId, colour)
        api.payBill(payRequest).enqueue(object : Callback<Unit> {
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                println("kak failnah" + t)
            }

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {

            }
        })

    }
}