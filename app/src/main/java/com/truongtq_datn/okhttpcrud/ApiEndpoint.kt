package com.truongtq_datn.okhttpcrud

data object ApiEndpoint {
    private var Url_Server = "https://burro-ideal-macaw.ngrok-free.app"

    val Endpoint_Account: String
        get() = "$Url_Server/account"

    val Endpoint_Account_Login: String
        get() = "$Endpoint_Account/login"

    val Endpoint_Account_Login_Biometric: String
        get() = "$Endpoint_Account/loginBiometric"

    val Endpoint_Account_Register: String
        get() = "$Endpoint_Account/register"

    val Endpoint_Account_GetAll: String
        get() = "$Endpoint_Account/getAllAccounts"

    val Endpoint_Door: String
        get() = "$Url_Server/door"

    val Endpoint_Door_Create: String
        get() = "$Endpoint_Door/create"

    val Endpoint_Door_GetAll: String
        get() = "$Endpoint_Door/getAll"

    val Endpoint_Door_AddAccountAccessDoor: String
        get() = "$Endpoint_Door/addAccountAccessDoor"

    val Endpoint_Ticket: String
        get() = "$Url_Server/ticket"

    val Endpoint_Ticket_Create: String
        get() = "$Endpoint_Ticket/create"

    val Endpoint_Ticket_IdAccount: String
        get() = "$Endpoint_Ticket/idAccount"

    val Endpoint_Ticket_IdDoor: String
        get() = "$Endpoint_Ticket/idDoor"

    val Endpoint_Ticket_GetAll: String
        get() = "$Endpoint_Ticket/getAll"
}