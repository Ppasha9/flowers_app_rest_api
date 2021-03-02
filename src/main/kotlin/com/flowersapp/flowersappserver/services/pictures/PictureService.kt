package com.flowersapp.flowersappserver.services.pictures

import com.flowersapp.flowersappserver.datatables.products.ProductToPictureRepository
import com.flowersapp.flowersappserver.datatables.products.ProductRepository
import com.flowersapp.flowersappserver.datatables.products.ProductToPicture
import com.flowersapp.flowersappserver.forms.products.UploadPictureForm
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.lang.RuntimeException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

@Service
class PictureService {
    @Autowired
    private lateinit var productRepository: ProductRepository
    @Autowired
    private lateinit var productToPictureRepository: ProductToPictureRepository

    private final var rootLocation: Path = Paths.get("picstorage")

    private var isInited = false

    @Transactional
    fun createFrom(form: UploadPictureForm): String? {
        if (!productRepository.existsById(form.productId)) {
            return "Product with id ${form.productId} doesn't exist"
        }

        store(form.uploadFile)
        productToPictureRepository.save(ProductToPicture(
            product = productRepository.findById(form.productId).get(),
            filename = form.uploadFile.originalFilename!!
        ))

        return null
    }

    fun canGetPictures(productId: Long): Boolean {
        return productToPictureRepository.existsByProductId(productId)
    }

    @Transactional
    fun getOnePicture(productId: Long): Resource {
        val pictureName = productToPictureRepository.findByProductId(productId)[0].filename
        return load(pictureName)
    }

    fun store(pic: MultipartFile) {
        if (!isInited) {
            init()
            isInited = true
        }

        Files.copy(pic.inputStream, rootLocation.resolve(pic.originalFilename!!))
    }

    fun load(filename: String): Resource {
        val file = rootLocation.resolve(filename)
        val resource = UrlResource(file.toUri())
        if (resource.exists() || resource.isReadable) {
            return resource
        } else {
            throw RuntimeException("Resource creating failed")
        }
    }

    fun deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile())
    }

    fun init() {
        Files.createDirectory(rootLocation)
    }

    fun loadFiles(): Stream<Path> {
        return Files.walk(rootLocation, 1)
            .filter { path: Path -> path != rootLocation }
            .map { path: Path -> (rootLocation::relativize)(path) }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(PictureService::class.java)
    }
}