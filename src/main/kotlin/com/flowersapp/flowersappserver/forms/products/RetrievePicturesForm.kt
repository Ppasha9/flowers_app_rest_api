package com.flowersapp.flowersappserver.forms.products

data class FileInfo(
    var filename: String,
    var url: String
)

data class RetrievePicturesForm(
    var pictures: ArrayList<FileInfo>
)