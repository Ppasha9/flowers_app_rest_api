package com.flowersapp.flowersappserver.forms.orders

data class ProductInOrderCuttedForm(
    val id: Long,
    val amount: Int
)

data class OrderFullForm(
    val id: Long,
    val price: Double,
    val fullPrice: Double,
    val status: String,
    val receiverName: String,
    val receiverPhone: String,
    val receiverEmail: String,
    val receiverStreet: String,
    val receiverHouseNum: String,
    val receiverApartmentNum: String,
    val deliveryComment: String,
    val deliveryMethod: String,
    val paymentMethod: String,
    val products: ArrayList<ProductInOrderCuttedForm>
)