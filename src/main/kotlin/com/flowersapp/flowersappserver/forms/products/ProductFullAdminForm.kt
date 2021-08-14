package com.flowersapp.flowersappserver.forms.products

import java.time.OffsetDateTime

data class ProductFullAdminForm(
    var id: Long,
    var name: String,
    var content: String,
    var size: String,
    var height: Double,
    var diameter: Double,
    var price: Double,
    var productFavouriteForUser: Boolean,
    var addDate: OffsetDateTime,
    var categories: ArrayList<String>,
    var tags: ArrayList<String>,
    var flowers: ArrayList<String>,
    var picFilename: String,
    var picUrl: String
)