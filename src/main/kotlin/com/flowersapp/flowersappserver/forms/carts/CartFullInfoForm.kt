package com.flowersapp.flowersappserver.forms.carts

import java.time.OffsetDateTime

data class CartFullInfoForm(
    var price: Double,
    var status: String,
    var receiverName: String,
    var receiverPhone: String,
    var receiverEmail: String,
    var receiverSurname: String,
    var receiverStreet: String,
    var receiverHouseNum: String,
    var receiverApartmentNum: String,
    var deliveryComment: String,
    var deliveryMethod: String,
    var paymentMethod: String,
    var deliveryDate: OffsetDateTime?
)