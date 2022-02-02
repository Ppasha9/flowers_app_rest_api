package com.flowersapp.flowersappserver.forms.orders

data class OrderFullAdminForm (
    var id: Long,
    var body: OrderFullForm,
    var picFilename: String,
    var picUrl: String
)