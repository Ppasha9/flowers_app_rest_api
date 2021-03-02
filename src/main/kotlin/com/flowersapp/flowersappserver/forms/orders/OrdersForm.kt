package com.flowersapp.flowersappserver.forms.orders

import com.flowersapp.flowersappserver.datatables.carts.DeliveryMethod

data class ProductInOrderForm(
    val id: Long,
    val name: String,
    val price: Double,
    val amount: Int
)

data class OrderCuttedForm(
    val id: Long,
    val fullPrice: Double,
    val status: String,
    val deliveryMethod: String,
    val products: ArrayList<ProductInOrderForm>
)

data class OrdersForm(
    val orders: ArrayList<OrderCuttedForm>
)