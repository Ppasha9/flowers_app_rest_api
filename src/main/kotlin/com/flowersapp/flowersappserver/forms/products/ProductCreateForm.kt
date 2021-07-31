package com.flowersapp.flowersappserver.forms.products

data class ProductCreateForm(
    var name: String,
    var content: String,
    var size: String,
    var height: Double,
    var diameter: Double,
    var price: Double,
    var categories: ArrayList<String>? = null,
    var tags: ArrayList<String>? = null,
    var flowers: ArrayList<String>? = null
)