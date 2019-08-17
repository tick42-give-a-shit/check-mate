package com.example.checkmate.data

import com.example.checkmate.api.Webservice
import com.example.checkmate.data.model.LoggedInUser

class MainActivityRepository(private val api: Webservice) {

    fun sendBillPhoto(photo: String) {
        api.addBillPhoto(photo)
    }

}