package com.flowersapp.flowersappserver.controllers

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.forms.authorization.UserAdminPanelForm
import com.flowersapp.flowersappserver.forms.products.*
import com.flowersapp.flowersappserver.services.products.ProductService
import com.flowersapp.flowersappserver.services.users.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
class AdminController {
    @Autowired
    private lateinit var userService: UserService

    /*** PRODUCTS REQUESTS: "/api/admin/products/..." ***/
    @Autowired
    private lateinit var productService: ProductService

    /* `getList` request from react-admin */
    @GetMapping("/products")
    fun getProductsList(
        @RequestParam(name = "range", required = false) range: ArrayList<Long>?,
        @RequestParam(name = "indices", required = false) indices: ArrayList<Long>?
    ): ResponseEntity<Any> {
        logger.debug("ADMIN PANEL: [PRODUCTS] [GET] - getList. Arguments: range: $range; indices: $indices")

        if (indices != null) {
            logger.debug("ADMIN PANEL: [PRODUCTS] [GET] - getList. Get products by indices=$indices")

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

        val products = productService.getByRange(range)
        val productsForms = products.map { productService.getFullAdminForm(it) } as ArrayList<ProductFullAdminForm>
        val headers = HttpHeaders()
        headers.add("Access-Control-Expose-Headers", "Content-Range");
        headers.add("Content-Range", productService.getTotalNum().toString())
        return ResponseEntity(productsForms, headers, HttpStatus.OK)
    }

    /* `create` request from react-admin */
    @PreAuthorize(Constants.ADMIN_AUTHORIZED_AUTHORITY)
    @PostMapping("/products")
    fun createProduct(
        @RequestParam(name = "name", required = true) name: String,
        @RequestParam(name = "content", required = true) content: String,
        @RequestParam(name = "size", required = true) size: String,
        @RequestParam(name = "height", required = true) height: Double,
        @RequestParam(name = "diameter", required = true) diameter: Double,
        @RequestParam(name = "price", required = true) price: Double,
        @RequestParam(name = "categories", required = true) categories: ArrayList<String>,
        @RequestParam(name = "tags", required = true) tags: ArrayList<String>,
        @RequestParam(name = "flowers", required = true) flowers: ArrayList<String>,
        @RequestParam(name = "picture", required = true) picture: MultipartFile
    ): ResponseEntity<Any> {
        val productForm = ProductCreateAdminForm(
            name = name,
            content = content,
            size = size,
            height = height,
            diameter = diameter,
            price = price,
            categories = categories,
            tags = tags,
            flowers = flowers,
            picture = picture
        )

        logger.debug("ADMIN PANEL: [PRODUCTS] [POST] - create. Arguments: $productForm")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)
        if (currUser.userType.code != Constants.ADMIN_USER_TYPE_CODE) {
            return ResponseEntity("Only admin can create products", HttpStatus.FORBIDDEN)
        }

        val err = productService.createFromAdmin(productForm)
        if (err != null) {
            return ResponseEntity(err, HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(ProductCreateResultForm(id = productService.findByName(productForm.name)!!.id!!), HttpStatus.CREATED)
    }

    /* `getOne` request from react-admin */
    @GetMapping("/products/{id}")
    fun getOneProduct(
        @PathVariable(name = "id", required = true) id: Long
    ): ResponseEntity<Any> {
        logger.debug("ADMIN PANEL: [PRODUCTS] [GET] - getOne. Id: $id")

        val productOptional = productService.findById(id)
        if (!productOptional.isPresent) {
            return ResponseEntity("Not found product by id=$id", HttpStatus.NOT_FOUND)
        }

        return ResponseEntity.ok(productService.getFullAdminForm(productOptional.get()))
    }

    /* `update` request from react-admin */
    @PreAuthorize(Constants.ADMIN_AUTHORIZED_AUTHORITY)
    @PutMapping("/products/{id}")
    fun updateOneProduct(
        @PathVariable(name = "id", required = true) id: Long,
        @Valid @RequestBody productUpdateForm: ProductCreateForm?
    ): ResponseEntity<Any> {
        logger.debug("ADMIN PANEL: [PRODUCTS] [PUT] - update. Id: $id, form: ${productUpdateForm.toString()}")

        if (productUpdateForm == null) {
            return ResponseEntity("Invalid request form.", HttpStatus.BAD_REQUEST)
        }

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)
        if (currUser.userType.code != Constants.ADMIN_USER_TYPE_CODE) {
            return ResponseEntity("Only admin can patch products", HttpStatus.FORBIDDEN)
        }

        val err = productService.patchWith(id, productUpdateForm)
        return if (err == null) ResponseEntity.ok(productService.getFullAdminForm(productService.findById(id).get()))
            else ResponseEntity(err, HttpStatus.BAD_REQUEST)
    }

    /* `delete` request from react-admin */
    @PreAuthorize(Constants.ADMIN_AUTHORIZED_AUTHORITY)
    @DeleteMapping("/products/{id}")
    fun deleteForeverOneProduct(
        @PathVariable(name = "id", required = true) id: Long
    ): ResponseEntity<Any> {
        logger.debug("ADMIN PANEL: [PRODUCTS] [DELETE] - delete. Id: $id")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)
        if (currUser.userType.code != Constants.ADMIN_USER_TYPE_CODE) {
            return ResponseEntity("Only admin can patch products", HttpStatus.FORBIDDEN)
        }

        val err = productService.deleteProductForever(id)
        return if (err == null) ResponseEntity.ok("Product was deleted successfully")
            else ResponseEntity(err, HttpStatus.BAD_REQUEST)
    }

    /* `deleteMany` request from react-admin */
    @PreAuthorize(Constants.ADMIN_AUTHORIZED_AUTHORITY)
    @DeleteMapping("/products")
    fun deleteForeverManyProducts(
        @RequestParam(name = "indices", required = true) indices: ArrayList<Long>
    ): ResponseEntity<Any> {
        logger.debug("ADMIN PANEL: [PRODUCTS] [DELETE] - deleteMany. Indices: $indices")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)
        if (currUser.userType.code != Constants.ADMIN_USER_TYPE_CODE) {
            return ResponseEntity("Only admin can patch products", HttpStatus.FORBIDDEN)
        }

        val err = productService.deleteProductsForever(indices)
        return if (err == null) ResponseEntity.ok("Products was deleted successfully")
            else ResponseEntity(err, HttpStatus.BAD_REQUEST)
    }

    /***  USERS REQUESTS: "/api/admin/users/..." ***/
    /* `getList` request from react-admin */
    @GetMapping("/users")
    fun getUsersList(
        @RequestParam(name = "range", required = false) range: ArrayList<Long>?
    ): ResponseEntity<Any> {
        logger.debug("ADMIN PANEL: [USERS] [GET] - getList. Arguments: range: $range")

        val users = userService.getByRange(range)
        val usersForms = users.map { user -> userService.getMeAdminPanelForm(user) } as ArrayList<UserAdminPanelForm>
        val headers = HttpHeaders()
        headers.add("Access-Control-Expose-Headers", "Content-Range");
        headers.add("Content-Range", userService.getTotalNumCasual().toString())
        return ResponseEntity(usersForms, headers, HttpStatus.OK)
    }

    /***  ADMIN USERS REQUESTS: "/api/admin/admins/..." ***/
    /* `getList` request from react-admin */
    @GetMapping("/admins")
    fun getAdminsUsersList(
        @RequestParam(name = "range", required = false) range: ArrayList<Long>?
    ): ResponseEntity<Any> {
        logger.debug("ADMIN PANEL: [ADMINS] [GET] - getList. Arguments: range: $range")

        val users = userService.getAdminsByRange(range)
        val usersForms = users.map { user -> userService.getMeAdminPanelForm(user) } as ArrayList<UserAdminPanelForm>
        val headers = HttpHeaders()
        headers.add("Access-Control-Expose-Headers", "Content-Range");
        headers.add("Content-Range", userService.getTotalNumAdmin().toString())
        return ResponseEntity(usersForms, headers, HttpStatus.OK)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(AdminController::class.java)
    }
}