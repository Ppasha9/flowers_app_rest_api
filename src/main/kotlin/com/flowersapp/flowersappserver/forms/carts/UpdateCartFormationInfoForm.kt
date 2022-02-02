package com.flowersapp.flowersappserver.forms.carts

import java.time.OffsetDateTime

data class UpdateCartFormationInfoForm(
    var receiverName: String? = null,
    var receiverPhone: String? = null,
    var receiverEmail: String? = null,
    var receiverSurname: String? = null,
    var receiverStreet: String? = null,
    var receiverHouseNum: String? = null,
    var receiverApartmentNum: String? = null,
    var deliveryComment: String? = null,
    var deliveryMethod: String? = null,
    var paymentMethod: String? = null,
    var deliveryDate: OffsetDateTime? = null
)