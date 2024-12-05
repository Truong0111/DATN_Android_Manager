package com.truongtq_datn.model

import com.google.gson.annotations.SerializedName

data class DoorResponse(
    @SerializedName("idDoor") val idDoor: String,
    @SerializedName("idAccountCreate") val idAccountCreate: String,
    @SerializedName("position") val position: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("lastUpdate") val lastUpdate: String
)