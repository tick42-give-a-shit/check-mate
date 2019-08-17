package com.example.checkmate.data.model

data class BillSession(
    val billId: String,
    val items: ArrayList<InitialItem>,
    val myColor: String,
    val restaurant: String,
    val total: TotalAmount,
    val billPhoto: String?
)