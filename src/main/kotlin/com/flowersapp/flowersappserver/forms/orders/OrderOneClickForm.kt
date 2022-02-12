package com.flowersapp.flowersappserver.forms.orders

import com.flowersapp.flowersappserver.forms.carts.ProductParameterForm
import java.time.OffsetDateTime


data class OrderOneClickForm(
    var productId: Long,
    var parameters: ArrayList<ProductParameterForm>? = null,
    val receiverName: String,
    val receiverSurname: String,
    val receiverPhone: String,
    val receiverEmail: String,
    val receiverStreet: String,
    val receiverHouseNum: String,
    val receiverApartmentNum: String,
    val deliveryComment: String,
    val deliveryMethod: String,
    val paymentMethod: String,
    var deliveryDate: OffsetDateTime? = null
)