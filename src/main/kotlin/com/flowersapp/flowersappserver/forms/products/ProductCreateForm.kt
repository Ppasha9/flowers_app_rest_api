package com.flowersapp.flowersappserver.forms.products

data class ProductCreateForm(
    var name: String,
    var content: String,
    var price: Double,
    var parameters: ArrayList<ProductParameterForm>? = null,
    var categories: ArrayList<String>? = null,
    var tags: ArrayList<String>? = null,
    var flowers: ArrayList<String>? = null
)