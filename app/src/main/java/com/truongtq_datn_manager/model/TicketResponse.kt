package com.truongtq_datn_manager.model

import com.google.gson.annotations.SerializedName

data class TicketResponse(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("positionDoor") val positionDoor: String,
    @SerializedName("idTicket") val idTicket: String,
    @SerializedName("idDoor") val idDoor: String,
    @SerializedName("idAccount") val idAccount: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("isAccept") val isAccept: Boolean,
)