package com.truongtq_datn.firebase

class User(
    var idAccount: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var refId: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var password: String = "",
    var arrDoor: MutableList<String> = mutableListOf(),
    var role: MutableList<String> = mutableListOf("user")
) {

    fun getFullName(): String {
        return "$firstName $lastName"
    }
}