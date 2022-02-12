package com.flowersapp.flowersappserver.forms.compilations

import com.flowersapp.flowersappserver.forms.products.ProductFullAdminForm

data class CompilationCuttedInfoForm(
    var id: Long,
    var name: String,
    var picFilename: String,
    var picUrl: String
)

data class CompilationFullInfoForm(
    var id: Long,
    var name: String,
    var products: ArrayList<ProductFullAdminForm>,
    var picFilename: String,
    var picUrl: String
)
