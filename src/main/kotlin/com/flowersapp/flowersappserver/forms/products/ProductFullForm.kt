package com.flowersapp.flowersappserver.forms.products

import java.time.OffsetDateTime

data class ProductFullForm(
    var id: Long,
    var name: String,
    var description: String,
    var price: Double,
    var addDate: OffsetDateTime,
    var categories: ArrayList<String>,
    var tags: ArrayList<String>
)