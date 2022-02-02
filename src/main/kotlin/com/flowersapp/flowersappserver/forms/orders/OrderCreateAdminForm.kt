package com.flowersapp.flowersappserver.forms.orders

import com.flowersapp.flowersappserver.forms.products.ProductParameterForm
import java.time.OffsetDateTime

data class SelectedProductInOrderAdminForm (
    var id: Long,
    var parameters: ArrayList<ProductParameterForm> = arrayListOf()
)

data class OrderCreateAdminForm (
    var receiverName: String = "",
    var receiverSurname: String = "",
    var receiverPhone: String = "",
    var receiverEmail: String = "",
    var receiverStreet: String = "",
    var receiverHouseNum: String = "",
    var receiverApartmentNum: String = "",
    var deliveryComment: String = "",
    var deliveryMethod: String = "",
    var paymentMethod: String = "",
    var shortDescription: String = "",
    var productsDescription: String = "",
    var price: Double = 0.0,
    var deliveryDate: OffsetDateTime? = null,
    var columnName: String = "",
    var selectedProductsDescrs: ArrayList<SelectedProductInOrderAdminForm> = arrayListOf()
)