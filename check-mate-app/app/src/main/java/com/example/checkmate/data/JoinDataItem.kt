package com.example.checkmate.data

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.example.checkmate.data.Rect

@JsonIgnoreProperties
data class JoinDataItem(
    val position: Rect,
    val name: String?,
    val quantity: Float?,
    val amount: Float?,
    val unitPrice: Float?,
    var clicked: Boolean?
)

