package com.flowersapp.flowersappserver.forms.products

data class ProductCuttedForm(
    var id: Long,
    var name: String,
    var price: Double,
    var productFavouriteForUser: Boolean
)