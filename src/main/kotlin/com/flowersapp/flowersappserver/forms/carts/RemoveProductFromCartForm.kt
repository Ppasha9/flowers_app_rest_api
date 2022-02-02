package com.flowersapp.flowersappserver.forms.carts

data class RemoveProductFromCartForm(
    var productId: Long,
    var parameters: ArrayList<ProductParameterForm>? = null,
    var permanently: Boolean? = null
)
