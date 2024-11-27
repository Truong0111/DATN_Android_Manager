package com.truongtq_datn_manager.model

import com.google.gson.annotations.SerializedName

class ResponseMessage(
    @SerializedName("message") val message: String
)