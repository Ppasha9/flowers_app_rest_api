package com.flowersapp.flowersappserver.forms.carts

data class CartFullInfoForm(
    var price: Double,
    var status: String,
    var receiverName: String,
    var receiverPhone: String,
    var receiverEmail: String,
    var receiverStreet: String,
    var receiverHouseNum: String,
    var receiverApartmentNum: String,
    var deliveryComment: String,
    var deliveryMethod: String,
    var paymentMethod: String
)