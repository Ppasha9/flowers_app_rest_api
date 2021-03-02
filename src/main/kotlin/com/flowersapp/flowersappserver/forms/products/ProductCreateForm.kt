package com.flowersapp.flowersappserver.forms.products

import javax.validation.constraints.NotBlank

data class ProductCreateForm(
    @field:NotBlank
    var name: String,

    @field:NotBlank
    var description: String,

    var price: Double,

    var categories: ArrayList<String>? = null
)