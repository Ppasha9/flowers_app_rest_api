package com.flowersapp.flowersappserver.forms.carts

data class ProductInCartForm(
    val id: Long,
    val amount: Int
)

data class CartAllProductsForm(
    val products: ArrayList<ProductInCartForm>
)