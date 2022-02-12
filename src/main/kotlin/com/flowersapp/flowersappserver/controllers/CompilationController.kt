package com.flowersapp.flowersappserver.controllers

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.forms.products.UploadCompilationPictureForm
import com.flowersapp.flowersappserver.services.compilations.CompilationService
import com.flowersapp.flowersappserver.services.pictures.PictureService
import com.flowersapp.flowersappserver.services.users.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/compilation")
class CompilationController {
    @Autowired
    private lateinit var compilationService: CompilationService
    @Autowired
    private lateinit var pictureService: PictureService
    @Autowired
    private lateinit var userService: UserService

    @PreAuthorize(Constants.ADMIN_AUTHORIZED_AUTHORITY)
    @PostMapping("")
    fun createCompilation(
        @RequestParam(name = "name", required = true) name: String,
        @RequestParam(name = "picture", required = true) picture: MultipartFile
    ): ResponseEntity<Any> {
        logger.debug("Creating compilation with name $name")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)
        if (currUser.userType.code != Constants.ADMIN_USER_TYPE_CODE) {
            return ResponseEntity("Only admin can create products", HttpStatus.FORBIDDEN)
        }

        return try {
            val compilation = compilationService.createCompilation(name)
            pictureService.createForCompilationFrom(
                UploadCompilationPictureForm(
                    compilationId = compilation.id!!,
                    uploadFile = picture
                )
            )
            ResponseEntity(compilation.id!!, HttpStatus.CREATED)
        } catch (e: RuntimeException) {
            ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        }
    }

    @PreAuthorize(Constants.ADMIN_AUTHORIZED_AUTHORITY)
    @PostMapping("/add-product")
    fun addProductToCompilation(
        @RequestParam(name = "productId", required = true) productId: Long,
        @RequestParam(name = "compilationId", required = true) compilationId: Long
    ): ResponseEntity<Any> {
        logger.debug("Add product with id $productId to compilation with id $compilationId")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)
        if (currUser.userType.code != Constants.ADMIN_USER_TYPE_CODE) {
            return ResponseEntity("Only admin can create products", HttpStatus.FORBIDDEN)
        }

        try {
            compilationService.addProductToCompilation(productId, compilationId)
        } catch (e: RuntimeException) {
            return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity.ok("Product with id $productId was successfully added to compilation with id $compilationId")
    }

    @GetMapping("/full-info/{id}")
    fun getCompilationFullInfo(
        @PathVariable(name = "id", required = true) compilationId: Long
    ): ResponseEntity<Any> {
        logger.debug("Getting info about compilation with id $compilationId")

        return try {
            val res = compilationService.getCompilationFullInfo(compilationId)
            ResponseEntity.ok(res)
        } catch (e: RuntimeException) {
            ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/all")
    fun getAllCompilationsCuttedForm(): ResponseEntity<Any> {
        logger.debug("Getting all compilations with cutted forms")

        return try {
            val res = compilationService.getAllCompilationsCuttedForms()
            ResponseEntity.ok(res)
        } catch (e: RuntimeException) {
            ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/pictures/{filename}")
    fun downloadFile(@PathVariable("filename", required = true) filename: String): ResponseEntity<Resource> {
        val pictureResource = pictureService.load(filename)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${pictureResource.filename}\"")
            .body(pictureResource)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CompilationController::class.java)
    }
}