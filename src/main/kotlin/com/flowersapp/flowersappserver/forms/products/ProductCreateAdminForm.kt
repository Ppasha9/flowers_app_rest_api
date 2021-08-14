package com.flowersapp.flowersappserver.forms.products

import org.springframework.web.multipart.MultipartFile

data class ProductCreateAdminForm(
    var name: String,
    var content: String,
    var size: String,
    var height: Double,
    var diameter: Double,
    var price: Double,
    var categories: ArrayList<String>? = null,
    var tags: ArrayList<String>? = null,
    var flowers: ArrayList<String>? = null,
    var picture: MultipartFile? = null
)