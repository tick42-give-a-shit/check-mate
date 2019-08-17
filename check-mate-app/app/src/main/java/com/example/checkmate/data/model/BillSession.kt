package com.example.checkmate.data.model

import com.fasterxml.jackson.annotation.JsonProperty

data class BillSession(
    val id: Long,
    val items: Array<InitialItem>,
    val color: String,
    val restaurant: String,
    val total: TotalAmount,
    val base64: String?
)