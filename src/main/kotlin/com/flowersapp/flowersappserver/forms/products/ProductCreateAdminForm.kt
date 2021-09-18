package com.flowersapp.flowersappserver.forms.products

import com.flowersapp.flowersappserver.datatables.products.ProductParameter
import org.springframework.web.multipart.MultipartFile

data class ProductCreateAdminForm(
    var name: String,
    var content: String,
    var price: Double,
    var parameters: ArrayList<ProductParameter>? = null,
    var categories: ArrayList<String>? = null,
    var tags: ArrayList<String>? = null,
    var flowers: ArrayList<String>? = null,
    var picture: MultipartFile? = null
)