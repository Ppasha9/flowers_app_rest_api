package com.flowersapp.flowersappserver.forms.products

import org.springframework.web.multipart.MultipartFile

data class UploadProductPictureForm(
    var productId: Long,
    var uploadFile: MultipartFile
)

data class UploadOrderPictureForm(
    var orderId: Long,
    var uploadFile: MultipartFile
)

data class UploadCompilationPictureForm(
    var compilationId: Long,
    var uploadFile: MultipartFile
)