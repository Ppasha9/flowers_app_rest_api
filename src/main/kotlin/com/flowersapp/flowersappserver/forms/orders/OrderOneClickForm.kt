package com.flowersapp.flowersappserver.forms.orders

data class OrderOneClickForm(
    val receiverName: String,
    val receiverSurname: String,
    val receiverPhone: String,
    val receiverEmail: String,
    val receiverStreet: String,
    val receiverHouseNum: String,
    val receiverApartmentNum: String,
    val deliveryComment: String,
    val deliveryMethod: String,
    val paymentMethod: String
)