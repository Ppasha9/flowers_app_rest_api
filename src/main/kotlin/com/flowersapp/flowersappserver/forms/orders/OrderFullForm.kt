package com.flowersapp.flowersappserver.forms.orders

import com.flowersapp.flowersappserver.forms.carts.ProductParameterForm
import java.time.OffsetDateTime

data class ProductInOrderCuttedForm(
    val id: Long,
    val name: String,
    val amount: Int,
    val parameters: ArrayList<ProductParameterForm>
)

data class OrderFullForm(
    val id: Long,
    val price: Double,
    val fullPrice: Double,
    val status: String,
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
    val products: ArrayList<ProductInOrderCuttedForm>,
    var shortDescription: String,
    var productsDescription: String,
    var deliveryDate: OffsetDateTime?
)