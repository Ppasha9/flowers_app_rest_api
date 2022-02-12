package com.flowersapp.flowersappserver.controllers

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.datatables.products.ProductToPictureRepository
import com.flowersapp.flowersappserver.forms.orders.OrderOneClickForm
import com.flowersapp.flowersappserver.forms.products.*
import com.flowersapp.flowersappserver.services.orders.OrderService
import com.flowersapp.flowersappserver.services.pictures.PictureService
import com.flowersapp.flowersappserver.services.products.ProductService
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
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import javax.validation.Valid

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/product")
class ProductController {
    @Autowired
    private lateinit var productService: ProductService
    @Autowired
    private lateinit var userService: UserService
    @Autowired
    private lateinit var pictureService: PictureService
    @Autowired
    private lateinit var productToPictureRepository: ProductToPictureRepository
    @Autowired
    private lateinit var orderService: OrderService

    @GetMapping("")
    fun filterProducts(
        @RequestParam(name = "range", required = false) range: ArrayList<Long>?,
        @RequestParam(name = "limit", required = false) limit: Int?,
        @RequestParam(name = "substr", required = false) substring: String?,
        @RequestParam(name = "min-price", required = false) minPrice: Int?,
        @RequestParam(name = "max-price", required = false) maxPrice: Int?,
        @RequestParam(name = "category", required = false) category: String?,
        @RequestParam(name = "group-num", required = false) groupNum: Int?,
        @RequestParam(name = "tags", required = false) tags: String?,
        @RequestParam(name = "flowers", required = false) flowers: String?,
        @RequestParam(name = "indices", required = false) indices: ArrayList<Long>?
    ): ResponseEntity<Any> {
        logger.debug("Filter products")

        if (indices != null) {
            logger.debug("Get products by indices=$indices")

            val res = arrayListOf<ProductFullForm>()
            indices.forEach {
                val productOptional = productService.findById(it)
                if (!productOptional.isPresent) {
                    return ResponseEntity("Not found product by id=$it", HttpStatus.NOT_FOUND)
                }
                res.add(productService.getFullForm(productOptional.get()))
            }

            return ResponseEntity.ok(res)
        }

        logger.debug("Params: range=$range limit=$limit, subsrt=$substring, min-price=$minPrice, max-price=$maxPrice," +
            " category=$category, tags=$tags, flowers=$flowers")

        val filteredProducts = productService.filter(range, limit, substring, minPrice, maxPrice, category, groupNum, tags, flowers)
        if (filteredProducts.isEmpty()) {
            return ResponseEntity("Not found products with these params", HttpStatus.NOT_FOUND)
        }

        val productsForms = filteredProducts.map { productService.getFullForm(it) } as ArrayList<ProductFullForm>
        return ResponseEntity.ok(ProductsFilterForm(products = productsForms))
    }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable(name = "id", required = true) productId: Long): ResponseEntity<Any> {
        logger.debug("Get product by id=$productId")

        val productOptional = productService.findById(productId)
        if (!productOptional.isPresent) {
            return ResponseEntity("Not found product by id=$productId", HttpStatus.NOT_FOUND)
        }

        return ResponseEntity.ok(productService.getFullForm(productOptional.get()))
    }

    @PreAuthorize(Constants.ADMIN_AUTHORIZED_AUTHORITY)
    @PostMapping("")
    fun createProduct(@Valid @RequestBody productForm: ProductCreateForm?): ResponseEntity<Any> {
        logger.debug("Creating new product")
        logger.debug("Create form: ${productForm.toString()}")

        if (productForm == null) {
            return ResponseEntity("Invalid request form.", HttpStatus.BAD_REQUEST)
        }

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)
        if (currUser.userType.code != Constants.ADMIN_USER_TYPE_CODE) {
            return ResponseEntity("Only admin can create products", HttpStatus.FORBIDDEN)
        }

        val err = productService.createFrom(productForm)
        if (err != null) {
            return ResponseEntity(err, HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(ProductCreateResultForm(id = productService.findByName(productForm.name)!!.id!!), HttpStatus.CREATED)
    }

    @PreAuthorize(Constants.ADMIN_AUTHORIZED_AUTHORITY)
    @PatchMapping("/{id}")
    fun patchProduct(
        @PathVariable(name = "id", required = true) productId: Long,
        @Valid @RequestBody productForm: ProductCreateForm?
    ): ResponseEntity<Any> {
        logger.debug("Patching product with id ")
        logger.debug("Patch form: ${productForm.toString()}")

        if (productForm == null) {
            return ResponseEntity("Invalid request form.", HttpStatus.BAD_REQUEST)
        }

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)
        if (currUser.userType.code != Constants.ADMIN_USER_TYPE_CODE) {
            return ResponseEntity("Only admin can patch products", HttpStatus.FORBIDDEN)
        }

        val err = productService.patchWith(productId, productForm)
        return if (err == null) ResponseEntity.ok("Product was successfully patched")
            else ResponseEntity(err, HttpStatus.BAD_REQUEST)
    }

    @PreAuthorize(Constants.ADMIN_AUTHORIZED_AUTHORITY)
    @PostMapping("/upload-picture")
    fun uploadProductPicture(
        @RequestParam("productId", required = true) productId: Long,
        @RequestParam("uploadFile", required = true) uploadFile: MultipartFile
    ): ResponseEntity<Any> {
        logger.debug("Uploading picture for product $productId")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)
        if (currUser.userType.code != Constants.ADMIN_USER_TYPE_CODE) {
            return ResponseEntity("Only admin can patch products", HttpStatus.FORBIDDEN)
        }

        val form = UploadProductPictureForm(productId = productId, uploadFile = uploadFile)
        val err = pictureService.createForProductFrom(form)
        return if (err == null) ResponseEntity.ok("Picture was successfully uploaded")
            else ResponseEntity(err, HttpStatus.BAD_REQUEST)
    }

    @GetMapping("/{id}/picture")
    fun retrieveProductPicture(@PathVariable("id", required = true) productId: Long): ResponseEntity<Any> {
        logger.debug("Retrieving picture for product with id $productId")

        if (!pictureService.canGetProductPictures(productId)) {
            return ResponseEntity("Product with id $productId doesn't have pictures", HttpStatus.NOT_FOUND)
        }

        val pictureResource = pictureService.getOneProductPicture(productId)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${pictureResource.filename}\"")
            .body(pictureResource)
    }

    @GetMapping("/pictures/{filename}")
    fun downloadFile(@PathVariable("filename", required = true) filename: String): ResponseEntity<Resource> {
        val pictureResource = pictureService.load(filename)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${pictureResource.filename}\"")
            .body(pictureResource)
    }

    @GetMapping("/{id}/all-pictures")
    fun retrieveProductAllPictures(@PathVariable("id", required = true) productId: Long): ResponseEntity<Any> {
        logger.debug("Retrieving all pictures for product with id $productId")

        if (!pictureService.canGetProductPictures(productId)) {
            return ResponseEntity("Product with id $productId doesn't have pictures", HttpStatus.NOT_FOUND)
        }

        val loadFiles = pictureService.loadFiles()
        val fileInfos: ArrayList<FileInfo> = arrayListOf()
        loadFiles.forEach {
            if (productToPictureRepository.existsByProductIdAndFilename(
                    productId = productId,
                    filename = it.fileName.toString()
                )) {
                fileInfos.add(
                    FileInfo(
                        filename = it.fileName.toString(),
                        url = MvcUriComponentsBuilder.fromMethodName(
                            ProductController::class.java,
                            "downloadFile",
                            it.fileName.toString()
                        ).build().toString()
                    )
                )
            }
        }

        return ResponseEntity.ok(RetrievePicturesForm(pictures = fileInfos))
    }

    @GetMapping("/one-per-category")
    fun getOneProductForEachCategory(): ResponseEntity<Any> {
        logger.debug("Getting one product per each category...")

        val res = productService.getOneProductForEachCategory()
        if (res.elems.isEmpty()) {
            return ResponseEntity("Not found products for each categories", HttpStatus.NOT_FOUND)
        }

        return ResponseEntity.ok(res)
    }

    @GetMapping("/max-price")
    fun getProductsMaxPriceByCategory(
        @RequestParam(name = "category", required = true) category: String
    ): ResponseEntity<Any> {
        logger.debug("Getting max price for products by category=$category")

        val maxPrice = productService.getMaxPriceByCategory(category) ?:
            return ResponseEntity("Not found products by category=$category", HttpStatus.NOT_FOUND)

        return ResponseEntity.ok(maxPrice)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/add-to-favourite")
    fun addProductToFavourite(
        @RequestParam("productId", required = true) productId: Long
    ): ResponseEntity<Any> {
        logger.debug("Adding product with id $productId to favourite")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        val err = productService.addProductToFavouriteForUser(productId, currUser)
        return if (err == null) ResponseEntity.ok("Product was successfully added to favourite")
            else ResponseEntity(err, HttpStatus.BAD_REQUEST)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/remove-from-favourite")
    fun removeProductFromFavourite(
        @RequestParam("productId", required = true) productId: Long
    ): ResponseEntity<Any> {
        logger.debug("Removing product with id $productId from favourite")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        val err = productService.removeProductFromFavouriteForUser(productId, currUser)
        return if (err == null) ResponseEntity.ok("Product was successfully removed from favourite")
            else ResponseEntity(err, HttpStatus.BAD_REQUEST)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("/all-favourite")
    fun getAllFavouriteProducts(): ResponseEntity<Any> {
        logger.debug("Getting all favourite products")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        val products = productService.getAllFavouriteProductsForUser(currUser)
        return ResponseEntity.ok(products.map { productService.getFullForm(it) }.toList())
    }

    @PostMapping("/one-click")
    fun buyByOneClick(
        @Valid @RequestBody reqBodyForm: OrderOneClickForm?
    ): ResponseEntity<Any> {
        logger.debug("Forming one-click order")

        if (reqBodyForm == null) {
            return ResponseEntity("Invalid request body", HttpStatus.BAD_REQUEST)
        }

        val currUser = userService.getCurrentAuthorizedUser()
        val err = orderService.createOneClickOrder(currUser, reqBodyForm)
        return if (err == null) {
            ResponseEntity.ok("One-click order was successfully created")
        } else {
            ResponseEntity(err, HttpStatus.BAD_REQUEST)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ProductController::class.java)
    }
}