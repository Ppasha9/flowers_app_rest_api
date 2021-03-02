package com.flowersapp.flowersappserver.forms.products

data class OneProductToCategoryCutted(
    var product: ProductCuttedForm,
    var category: String
)

data class OneProductPerCategoryCuttedForm(
    var elems: ArrayList<OneProductToCategoryCutted>
)
