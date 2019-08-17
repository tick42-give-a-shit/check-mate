package com.example.checkmate.data

import androidx.lifecycle.MutableLiveData
import com.example.checkmate.api.CreateBillRequest
import com.example.checkmate.api.JoinSessionRequest
import com.example.checkmate.api.Webservice
import com.example.checkmate.data.model.BillSession
import com.example.checkmate.data.model.LoggedInUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivityRepository(private val api: Webservice) {

    val currentBillSession: MutableLiveData<BillSession> by lazy {
        MutableLiveData<BillSession>()
    }

    fun joinPaymentSession(billId: String) {
        val joinSessionCall = api.joinBillSession(JoinSessionRequest(billId.toLong()))

        joinSessionCall.enqueue(object : Callback<BillSession> {
            override fun onFailure(call: Call<BillSession>, t: Throwable) {
                println("kak failnah" + t)
            }

            override fun onResponse(call: Call<BillSession>, response: Response<BillSession>) {
                currentBillSession.value = response.body()
            }
        })
    }

    fun sendBillPhoto(photo: String) {
        val createBillCall = api.createNewBill(CreateBillRequest(photo))

        createBillCall.enqueue(object : Callback<BillSession> {
            override fun onFailure(call: Call<BillSession>, t: Throwable) {
                println("kak failnah" + t)
            }

            override fun onResponse(call: Call<BillSession>, response: Response<BillSession>) {

                currentBillSession.value = response.body()
            }
        })
    }

}