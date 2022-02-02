package com.flowersapp.flowersappserver.forms.carts

data class ProductParameterForm(
    var parameterName: String? = null,
    var parameterValue: String? = null,
    var parameterPrice: Double? = null
)

data class AddProductToCartForm(
    var productId: Long,
    var parameters: ArrayList<ProductParameterForm>? = null
)
