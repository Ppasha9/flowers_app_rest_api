package com.flowersapp.flowersappserver.forms.delivery_address

data class DeliveryAddressForm(
    var name: String,
    var houseAddress: String,
    var flatNum: Int,
    var entranceNum: Int,
    var floorNum: Int,
    var comment: String? = null
)