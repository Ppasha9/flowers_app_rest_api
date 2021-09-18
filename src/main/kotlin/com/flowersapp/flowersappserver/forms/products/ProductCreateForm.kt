package com.flowersapp.flowersappserver.forms.products

import com.flowersapp.flowersappserver.datatables.products.ProductParameter

data class ProductCreateForm(
    var name: String,
    var content: String,
    var price: Double,
    var parameters: ArrayList<ProductParameter>? = null,
    var categories: ArrayList<String>? = null,
    var tags: ArrayList<String>? = null,
    var flowers: ArrayList<String>? = null
)