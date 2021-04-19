package com.flowersapp.flowersappserver.forms.products

data class ProductCreateForm(
    var name: String,
    var description: String,
    var price: Double,
    var categories: ArrayList<String>? = null,
    var tags: ArrayList<String>? = null
)