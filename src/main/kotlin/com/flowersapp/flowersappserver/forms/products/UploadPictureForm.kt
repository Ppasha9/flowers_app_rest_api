package com.flowersapp.flowersappserver.forms.products

import org.springframework.web.multipart.MultipartFile

data class UploadPictureForm(
    var productId: Long,
    var uploadFile: MultipartFile
)