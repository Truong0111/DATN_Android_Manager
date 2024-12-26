package com.truongtq_datn.model

import com.google.gson.annotations.SerializedName

data class AccountResponse(
    @SerializedName("idAccount") val idAccount: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("email") val email: String,
    @SerializedName("refId") val refId: String,
    @SerializedName("phoneNumber") val phoneNumber: String
)
