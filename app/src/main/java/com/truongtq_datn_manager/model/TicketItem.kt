package com.truongtq_datn_manager.model

data class TicketItem(
    val fullName: String,
    val positionDoor: String,
    val idTicket: String,
    val idDoor: String,
    val idAccount: String,
    val startTime: String,
    val endTime: String,
    val reason: String,
    val createdAt: String,
    val isAccept: Boolean,
)