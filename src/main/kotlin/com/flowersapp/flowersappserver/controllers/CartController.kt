package com.flowersapp.flowersappserver.controllers

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.forms.carts.UpdateCartFormationInfoForm
import com.flowersapp.flowersappserver.services.carts.CartService
import com.flowersapp.flowersappserver.services.orders.OrderService
import com.flowersapp.flowersappserver.services.products.ProductService
import com.flowersapp.flowersappserver.services.users.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/cart")
class CartController {
    @Autowired
    private lateinit var userService: UserService
    @Autowired
    private lateinit var cartService: CartService
    @Autowired
    private lateinit var productService: ProductService
    @Autowired
    private lateinit var orderService: OrderService

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/add-product")
    fun addProductToCart(@RequestParam(name = "productId", required = true) productId: Long): ResponseEntity<Any> {
        logger.debug("Adding product with id=$productId to cart")

        if (!productService.existsById(productId)) {
            return ResponseEntity("Not found product with id=$productId", HttpStatus.BAD_REQUEST)
        }

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        cartService.addProductToCart(currUser, productService.findById(productId).get())
        return ResponseEntity.ok("Product was successfully added")
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/remove-product")
    fun removeProduct(@RequestParam(name = "productId", required = true) productId: Long): ResponseEntity<Any> {
        logger.debug("Removing product with id=$productId to cart")

        if (!productService.existsById(productId)) {
            return ResponseEntity("Not found product with id=$productId", HttpStatus.BAD_REQUEST)
        }

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        cartService.removeProductFromCart(currUser, productService.findById(productId).get())
        return ResponseEntity.ok("Product was successfully removed")
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("/all-products")
    fun getCartProducts(): ResponseEntity<Any> {
        logger.debug("Getting all products in cart")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        return ResponseEntity.ok(cartService.getAllProductsInCartForm(currUser))
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("/is-product-in-cart")
    fun isProductInCart(@RequestParam(name = "productId", required = true) productId: Long): ResponseEntity<Any> {
        logger.debug("Checking if product with id=$productId is in cart")

        if (!productService.existsById(productId)) {
            return ResponseEntity("Not found product with id=$productId", HttpStatus.BAD_REQUEST)
        }

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        return ResponseEntity.ok(cartService.isProductInCart(currUser, productId))
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/clear")
    fun clearCart(): ResponseEntity<Any> {
        logger.debug("Clear the cart")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        cartService.clearCartForUser(currUser)
        return ResponseEntity.ok("Cart was successfully cleared")
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("/status")
    fun getStatus(): ResponseEntity<Any> {
        logger.debug("Getting cart status")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        return ResponseEntity.ok(cartService.getCartForUser(currUser).status.code)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/increase-status")
    fun increaseCartStatus(): ResponseEntity<Any> {
        logger.debug("Increasing status of cart")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        cartService.increaseStatusByUser(currUser)
        return ResponseEntity.ok("Cart status was successfully increased")
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/formation-info")
    fun updateCartFormationInfo(@Valid @RequestBody form: UpdateCartFormationInfoForm?): ResponseEntity<Any> {
        logger.debug("Updating cart formation info")

        if (form == null) {
            return ResponseEntity("Invalid request body", HttpStatus.BAD_REQUEST)
        }

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        cartService.updateCartFormationInfoFromForm(currUser, form)
        return ResponseEntity.ok("Cart formation info was successfully updated")
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("/full-info")
    fun getFullCartInformation(): ResponseEntity<Any> {
        logger.debug("Getting full information about cart")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        return ResponseEntity.ok(cartService.getFullInfoByUser(currUser))
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/create-order")
    fun createOrder(): ResponseEntity<Any> {
        logger.debug("Creating order from cart")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        orderService.createOrderFromCart(currUser)
        cartService.clearCartForUser(currUser)

        return ResponseEntity.ok("Order was successfully created")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CartController::class.java)
    }
}