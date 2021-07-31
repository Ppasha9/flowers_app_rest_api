package com.flowersapp.flowersappserver.services.carts

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.datatables.carts.*
import com.flowersapp.flowersappserver.datatables.products.Product
import com.flowersapp.flowersappserver.datatables.products.ProductToCart
import com.flowersapp.flowersappserver.datatables.products.ProductToCartRepository
import com.flowersapp.flowersappserver.datatables.users.User
import com.flowersapp.flowersappserver.datatables.users.UserRepository
import com.flowersapp.flowersappserver.forms.carts.CartAllProductsForm
import com.flowersapp.flowersappserver.forms.carts.CartFullInfoForm
import com.flowersapp.flowersappserver.forms.carts.ProductInCartForm
import com.flowersapp.flowersappserver.forms.carts.UpdateCartFormationInfoForm
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartService {
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var cartRepository: CartRepository
    @Autowired
    private lateinit var cartStatusRepository: CartStatusRepository
    @Autowired
    private lateinit var productToCartRepository: ProductToCartRepository
    @Autowired
    private lateinit var cartFormationInfoRepository: CartFormationInfoRepository

    @Transactional
    fun createNewCartForUser(user: User): Cart {
        val userCode = userRepository.findByEmail(user.email)!!.code
        val cart = Cart(
            userCode = userCode,
            status = cartStatusRepository.findByCode(Constants.CART_STATUS_DEFAULT)!!
        )

        cartRepository.saveAndFlush(cart)

        cartFormationInfoRepository.saveAndFlush(CartFormationInfo(cartId = cartRepository.findByUserCode(userCode)!!.id!!))

        return cart
    }

    @Transactional
    fun getCartForUser(user: User): Cart {
        if (cartRepository.existsByUserCode(user.code)) {
            return cartRepository.findByUserCode(user.code)!!
        }

        return createNewCartForUser(user)
    }

    @Transactional
    fun removeProductFromCart(user: User, product: Product, permanently: Boolean?) {
        val cart = getCartForUser(user)

        if (!productToCartRepository.existsByCartIdAndProductId(cart.id!!, product.id!!)) {
            return
        }

        val prToCart = productToCartRepository.findByCartIdAndProductId(cart.id!!, product.id!!)!!
        if (permanently != null && permanently == true) {
            productToCartRepository.delete(prToCart)
        } else {
            prToCart.amount--
            if (prToCart.amount == 0) {
                productToCartRepository.delete(prToCart)
            } else {
                productToCartRepository.save(prToCart)
            }
        }

        cart.price -= product.price
        cartRepository.saveAndFlush(cart)
    }

    @Transactional
    fun addProductToCart(user: User, product: Product) {
        val cart = getCartForUser(user)

        if (productToCartRepository.existsByCartIdAndProductId(cart.id!!, product.id!!)) {
            val ptc = productToCartRepository.findByCartIdAndProductId(cart.id!!, product.id!!)!!
            ptc.amount++
            productToCartRepository.saveAndFlush(ptc)
        } else {
            productToCartRepository.saveAndFlush(ProductToCart(
                product = product,
                cart = cart,
                amount = 1
            ))
        }

        cart.price += product.price
        cartRepository.saveAndFlush(cart)
    }

    @Transactional
    fun getAllProductsInCartForm(user: User): CartAllProductsForm {
        val res = CartAllProductsForm(products = arrayListOf())
        val cart = getCartForUser(user)

        productToCartRepository.findByCartId(cart.id!!).forEach {
            res.products.add(ProductInCartForm(id = it.product.id!!, amount = it.amount))
        }

        return res
    }

    @Transactional
    fun isProductInCart(user: User, productId: Long): Boolean {
        val cart = getCartForUser(user)
        return productToCartRepository.existsByCartIdAndProductId(cart.id!!, productId)
    }

    @Transactional
    fun clearCartForUser(user: User) {
        val cart = getCartForUser(user)
        productToCartRepository.findByCartId(cart.id!!).forEach {
            productToCartRepository.delete(it)
        }
        cart.price = 0.0
        cartRepository.saveAndFlush(cart)
    }

    @Transactional
    fun increaseStatusByUser(user: User) {
        val cart = getCartForUser(user)

        when (cart.status.code) {
            Constants.CART_STATUS_DEFAULT -> cart.status = cartStatusRepository.findByCode(Constants.CART_STATUS_RECEIVER_FORMATION)!!
            Constants.CART_STATUS_RECEIVER_FORMATION -> cart.status = cartStatusRepository.findByCode(Constants.CART_STATUS_SHIPPING_FORMATION)!!
            Constants.CART_STATUS_SHIPPING_FORMATION -> cart.status = cartStatusRepository.findByCode(Constants.CART_STATUS_PAYMENT_FORMATION)!!
            Constants.CART_STATUS_PAYMENT_FORMATION -> cart.status = cartStatusRepository.findByCode(Constants.CART_STATUS_DEFAULT)!!
        }

        cartRepository.saveAndFlush(cart)
    }

    @Transactional
    fun updateCartFormationInfoFromForm(user: User, form: UpdateCartFormationInfoForm) {
        val cart = getCartForUser(user)
        val cartFormationInfo = cartFormationInfoRepository.findByCartId(cart.id!!)!!

        cartFormationInfo.receiverName = if (form.receiverName.isNullOrBlank()) cartFormationInfo.receiverName else form.receiverName!!
        cartFormationInfo.receiverSurname = if (form.receiverSurname.isNullOrBlank()) cartFormationInfo.receiverSurname else form.receiverSurname!!
        cartFormationInfo.receiverPhone = if (form.receiverPhone.isNullOrBlank()) cartFormationInfo.receiverPhone else form.receiverPhone!!
        cartFormationInfo.receiverEmail = if (form.receiverEmail.isNullOrBlank()) cartFormationInfo.receiverEmail else form.receiverEmail!!
        cartFormationInfo.receiverApartmentNum = if (form.receiverApartmentNum.isNullOrBlank()) cartFormationInfo.receiverApartmentNum else form.receiverApartmentNum!!
        cartFormationInfo.receiverHouseNum = if (form.receiverHouseNum.isNullOrBlank()) cartFormationInfo.receiverHouseNum else form.receiverHouseNum!!
        cartFormationInfo.receiverStreet = if (form.receiverStreet.isNullOrBlank()) cartFormationInfo.receiverStreet else form.receiverStreet!!
        cartFormationInfo.deliveryComment = if (form.deliveryComment.isNullOrBlank()) cartFormationInfo.deliveryComment else form.deliveryComment!!
        cartFormationInfo.deliveryMethod = if (form.deliveryMethod.isNullOrBlank()) cartFormationInfo.deliveryMethod else DeliveryMethod.fromString(form.deliveryMethod!!)
        cartFormationInfo.paymentMethod = if (form.paymentMethod.isNullOrBlank()) cartFormationInfo.paymentMethod else PaymentMethod.fromString(form.paymentMethod!!)

        cartFormationInfoRepository.saveAndFlush(cartFormationInfo)
    }

    @Transactional
    fun getFullInfoByUser(user: User): CartFullInfoForm {
        val cart = getCartForUser(user)
        val cartFormationInfo = cartFormationInfoRepository.findByCartId(cart.id!!)!!
        return CartFullInfoForm(
            price = cart.price,
            status = cart.status.code,
            receiverName = cartFormationInfo.receiverName,
            receiverApartmentNum = cartFormationInfo.receiverApartmentNum,
            receiverEmail = cartFormationInfo.receiverEmail,
            receiverSurname = cartFormationInfo.receiverSurname,
            receiverHouseNum = cartFormationInfo.receiverHouseNum,
            receiverPhone = cartFormationInfo.receiverPhone,
            receiverStreet = cartFormationInfo.receiverStreet,
            deliveryComment = cartFormationInfo.deliveryComment,
            deliveryMethod = cartFormationInfo.deliveryMethod.name,
            paymentMethod = cartFormationInfo.paymentMethod.name
        )
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CartService::class.java)
    }
}