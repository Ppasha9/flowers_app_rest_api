package com.flowersapp.flowersappserver.forms.products

import com.flowersapp.flowersappserver.datatables.products.ProductParameter

data class ProductAddParametersAdminForm (
    var id: Long,
    var parameters: ArrayList<ProductParameter>
)