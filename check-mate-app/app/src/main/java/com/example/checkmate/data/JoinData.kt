package com.example.checkmate.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown=true)
data class JoinData(
    val id: String,
    val base64: String,
    val restaurant: String,
    val items: Array<JoinDataItem>,
    val total: JoinDataItem,
    val color: String?
)

