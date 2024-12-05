package com.truongtq_datn.model

import com.google.gson.annotations.SerializedName

class ResponseMessage(
    @SerializedName("message") val message: String
)