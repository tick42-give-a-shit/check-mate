package com.example.checkmate.data.model

import com.fasterxml.jackson.annotation.JsonProperty

data class BillSession(
    val billId: String,
    val items: Array<InitialItem>,
    val myColor: String,
    val restaurant: String,
    val total: TotalAmount,
    val billPhoto: String?
)