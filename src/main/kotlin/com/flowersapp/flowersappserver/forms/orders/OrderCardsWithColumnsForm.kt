package com.flowersapp.flowersappserver.forms.orders

data class OrderCardForm(
    val order: OrderFullAdminForm,
    val columnName: String
)

data class OrderCardsWithColumnsForm(
    val cards: ArrayList<OrderCardForm>,
    val columns: ArrayList<String>
)