package com.flowersapp.flowersappserver.forms.carts

data class UpdateCartFormationInfoForm(
    var receiverName: String? = null,
    var receiverPhone: String? = null,
    var receiverEmail: String? = null,
    var receiverStreet: String? = null,
    var receiverHouseNum: String? = null,
    var receiverApartmentNum: String? = null,
    var deliveryComment: String? = null,
    var deliveryMethod: String? = null,
    var paymentMethod: String? = null
)