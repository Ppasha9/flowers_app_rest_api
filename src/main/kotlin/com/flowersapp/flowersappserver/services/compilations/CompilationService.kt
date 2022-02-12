package com.flowersapp.flowersappserver.services.compilations

import com.flowersapp.flowersappserver.controllers.CompilationController
import com.flowersapp.flowersappserver.datatables.products.*
import com.flowersapp.flowersappserver.forms.compilations.CompilationCuttedInfoForm
import com.flowersapp.flowersappserver.forms.compilations.CompilationFullInfoForm
import com.flowersapp.flowersappserver.services.pictures.PictureService
import com.flowersapp.flowersappserver.services.products.ProductService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder

class CompilationServiceException(msg: String): RuntimeException(msg)

@Service
class CompilationService {
    @Autowired
    private lateinit var compilationRepository: CompilationRepository
    @Autowired
    private lateinit var compilationToPictureRepository: CompilationToPictureRepository
    @Autowired
    private lateinit var productToCompilationRepository: ProductToCompilationRepository
    @Autowired
    private lateinit var productRepository: ProductRepository
    @Autowired
    private lateinit var productService: ProductService
    @Autowired
    private lateinit var pictureService: PictureService

    @Throws(RuntimeException::class)
    @Transactional
    fun createCompilation(compilationName: String): Compilation {
        if (compilationRepository.existsByName(compilationName)) {
            throw CompilationServiceException("Compilation with name $compilationName already exists")
        }

        return compilationRepository.saveAndFlush(Compilation(name = compilationName))
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun addProductToCompilation(productId: Long, compilationId: Long) {
        if (!productRepository.existsById(productId)) {
            throw CompilationServiceException("Product with id $productId doesn't exist")
        }

        if (!compilationRepository.existsById(compilationId)) {
            throw CompilationServiceException("Compilation with id $compilationId doesn't exist")
        }

        productToCompilationRepository.saveAndFlush(ProductToCompilation(
            product = productRepository.findById(productId).get(),
            compilation = compilationRepository.findById(compilationId).get()
        ))
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun removeProductFromCompilation(productId: Long, compilationId: Long) {
        if (!productRepository.existsById(productId)) {
            throw CompilationServiceException("Product with id $productId doesn't exist")
        }

        if (!compilationRepository.existsById(compilationId)) {
            throw CompilationServiceException("Compilation with id $compilationId doesn't exist")
        }

        if (!productToCompilationRepository.existsByProductIdAndCompilationId(productId, compilationId)) {
            return
        }

        productToCompilationRepository.delete(productToCompilationRepository.findByProductIdAndCompilationId(productId, compilationId))
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun getCompilationFullInfo(compilationId: Long): CompilationFullInfoForm {
        if (!compilationRepository.existsById(compilationId)) {
            throw CompilationServiceException("Compilation with id $compilationId doesn't exist")
        }

        val compilation = compilationRepository.findById(compilationId).get()
        val res = CompilationFullInfoForm(
            id = compilationId,
            name = compilation.name,
            products = arrayListOf(),
            picFilename = "",
            picUrl = ""
        )
        productToCompilationRepository.findByCompilationId(compilationId).forEach {
            res.products.add(productService.getFullAdminForm(it.product))
        }

        if (!pictureService.canGetCompilationPictures(compilationId)) {
            return res
        }

        val loadFiles = pictureService.loadFiles()
        loadFiles.forEach {
            if (compilationToPictureRepository.existsByCompilationIdAndFilename(
                    compilationId = compilationId,
                    filename = it.fileName.toString()
                )) {
                res.picFilename = it.fileName.toString()
                res.picUrl = MvcUriComponentsBuilder.fromMethodName(
                    CompilationController::class.java,
                    "downloadFile",
                    it.fileName.toString()
                ).build().toString()
            }
        }
        return res
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun getCompilationCuttedInfoById(compilationId: Long): CompilationCuttedInfoForm {
        if (!compilationRepository.existsById(compilationId)) {
            throw CompilationServiceException("Compilation with id $compilationId doesn't exist")
        }

        val compilation = compilationRepository.findById(compilationId).get()
        val res = CompilationCuttedInfoForm(
            id = compilationId,
            name = compilation.name,
            picFilename = "",
            picUrl = ""
        )

        if (!pictureService.canGetCompilationPictures(compilationId)) {
            return res
        }

        val loadFiles = pictureService.loadFiles()
        loadFiles.forEach {
            if (compilationToPictureRepository.existsByCompilationIdAndFilename(
                    compilationId = compilationId,
                    filename = it.fileName.toString()
                )) {
                res.picFilename = it.fileName.toString()
                res.picUrl = MvcUriComponentsBuilder.fromMethodName(
                    CompilationController::class.java,
                    "downloadFile",
                    it.fileName.toString()
                ).build().toString()
            }
        }
        return res
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun getCompilationCuttedInfo(compilation: Compilation): CompilationCuttedInfoForm {
        val res = CompilationCuttedInfoForm(
            id = compilation.id!!,
            name = compilation.name,
            picFilename = "",
            picUrl = ""
        )

        if (!pictureService.canGetCompilationPictures(compilation.id!!)) {
            return res
        }

        val loadFiles = pictureService.loadFiles()
        loadFiles.forEach {
            if (compilationToPictureRepository.existsByCompilationIdAndFilename(
                    compilationId = compilation.id!!,
                    filename = it.fileName.toString()
                )) {
                res.picFilename = it.fileName.toString()
                res.picUrl = MvcUriComponentsBuilder.fromMethodName(
                    CompilationController::class.java,
                    "downloadFile",
                    it.fileName.toString()
                ).build().toString()
            }
        }
        return res
    }


    @Throws(RuntimeException::class)
    @Transactional
    fun getAllCompilationsCuttedForms(): ArrayList<CompilationCuttedInfoForm> {
        val res = arrayListOf<CompilationCuttedInfoForm>()
        compilationRepository.findAll().forEach {
            res.add(getCompilationCuttedInfo(it))
        }
        return res
    }
}