package com.flowersapp.flowersappserver.forms.products

import java.time.OffsetDateTime

data class ProductFullForm(
    var id: Long,
    var name: String,
    var content: String,
    var price: Double,
    var productFavouriteForUser: Boolean,
    var addDate: OffsetDateTime,
    var parameters: ArrayList<ProductParameterForm>,
    var categories: ArrayList<String>,
    var tags: ArrayList<String>,
    var flowers: ArrayList<String>
)