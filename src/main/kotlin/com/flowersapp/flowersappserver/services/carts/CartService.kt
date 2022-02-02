package com.flowersapp.flowersappserver.services.carts

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.datatables.carts.*
import com.flowersapp.flowersappserver.datatables.products.*
import com.flowersapp.flowersappserver.datatables.users.User
import com.flowersapp.flowersappserver.datatables.users.UserRepository
import com.flowersapp.flowersappserver.forms.carts.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

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
    private lateinit var productParametersForProductInCartRepository: ProductParametersForProductInCartRepository
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
    fun removeProductFromCart(user: User, product: Product, parameters: ArrayList<ProductParameterForm>, permanently: Boolean?) {
        val cart = getCartForUser(user)

        if (!productToCartRepository.existsByCartIdAndProductId(cart.id!!, product.id!!)) {
            return
        }

        val prToCartList = productToCartRepository.findByCartIdAndProductId(cart.id!!, product.id!!)
        var prToCart: ProductToCart? = null
        for (it in prToCartList) {
            if (areParametersEqual(getProductToCartParameters(it), parameters)) {
                prToCart = it
                break
            }
        }

        if (prToCart == null) {
            return
        }

        if (permanently != null && permanently == true) {
            val paramsList = productParametersForProductInCartRepository.findByProductToCartId(prToCart.id!!)
            paramsList.forEach {
                productParametersForProductInCartRepository.delete(it)
            }
            productToCartRepository.delete(prToCart)
        } else {
            prToCart.amount--
            if (prToCart.amount == 0) {
                val paramsList = productParametersForProductInCartRepository.findByProductToCartId(prToCart.id!!)
                paramsList.forEach {
                    productParametersForProductInCartRepository.delete(it)
                }
                productToCartRepository.delete(prToCart)
            } else {
                productToCartRepository.saveAndFlush(prToCart)
            }
        }

        var priceToSub = product.price
        for (param in parameters) {
            if (param.parameterPrice != null) {
                priceToSub += param.parameterPrice!!
            }
        }
        cart.price -= priceToSub
        cartRepository.saveAndFlush(cart)
    }

    @Transactional
    fun getProductToCartParameters(ptc: ProductToCart): ArrayList<ProductParameterForm> {
        if (!productParametersForProductInCartRepository.existsByProductToCartId(ptc.id!!)) {
            return arrayListOf()
        }

        val arr = productParametersForProductInCartRepository.findByProductToCartId(ptc.id!!)
        val res = arrayListOf<ProductParameterForm>()
        for (elem in arr) {
            res.add(ProductParameterForm(
                parameterPrice = elem.parameterPrice,
                parameterValue = elem.parameterValue,
                parameterName = elem.parameterName
            ))
        }

        return res
    }

    fun areParametersEqual(a1: ArrayList<ProductParameterForm>, a2: ArrayList<ProductParameterForm>): Boolean {
        if (a1.count() != a2.count()) {
            return false
        }

        for (e1 in a1) {
            var isContains = false
            for (e2 in a2) {
                if (e1.parameterPrice == e2.parameterPrice && e1.parameterName == e2.parameterName && e1.parameterValue == e2.parameterValue) {
                    isContains = true
                    break
                }
            }

            if (!isContains) {
                return false
            }
        }

        return true
    }

    @Transactional
    fun addProductToCart(user: User, product: Product, parameters: ArrayList<ProductParameterForm>) {
        val cart = getCartForUser(user)
        var isExistWithParams = false
        val isExistByCartAndProduct = productToCartRepository.existsByCartIdAndProductId(cart.id!!, product.id!!)

        if (isExistByCartAndProduct) {
            val ptcList = productToCartRepository.findByCartIdAndProductId(cart.id!!, product.id!!)
            for (ptc in ptcList) {
                val curParams = getProductToCartParameters(ptc)
                if (areParametersEqual(curParams, parameters)) {
                    isExistWithParams = true
                    ptc.amount++
                    productToCartRepository.saveAndFlush(ptc)
                    break
                }
            }
        }

        if (!isExistByCartAndProduct || !isExistWithParams) {
            val ptc = productToCartRepository.saveAndFlush(
                ProductToCart(
                    product = product,
                    cart = cart
                )
            )

            for (param in parameters) {
                productParametersForProductInCartRepository.saveAndFlush(
                    ProductParametersForProductInCart(
                        productToCart = ptc,
                        parameterName = param.parameterName,
                        parameterValue = param.parameterValue,
                        parameterPrice = param.parameterPrice
                    )
                )
            }
        }

        var priceToAdd = product.price
        for (param in parameters) {
            if (param.parameterPrice != null) {
                priceToAdd += param.parameterPrice!!
            }
        }
        cart.price += priceToAdd
        cartRepository.saveAndFlush(cart)
    }

    @Transactional
    fun getAllProductsInCartForm(user: User): CartAllProductsForm {
        val res = CartAllProductsForm(products = arrayListOf())
        val cart = getCartForUser(user)

        productToCartRepository.findByCartId(cart.id!!).forEach {
            val picf = ProductInCartForm(
                id = it.product.id!!,
                amount = it.amount,
                parameters = arrayListOf()
            )

            productParametersForProductInCartRepository.findByProductToCartId(it.id!!).forEach {
                picf.parameters?.add(ProductParameterForm(
                    parameterPrice = it.parameterPrice,
                    parameterValue = it.parameterValue,
                    parameterName = it.parameterName
                ))
            }

            res.products.add(picf)
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
            val paramsList = productParametersForProductInCartRepository.findByProductToCartId(it.id!!)
            paramsList.forEach { ppForPrInCart ->
                productParametersForProductInCartRepository.delete(ppForPrInCart)
            }
            productToCartRepository.delete(it)
        }
        cart.price = 0.0
        cart.status = cartStatusRepository.findByCode(Constants.CART_STATUS_DEFAULT)!!
        cartRepository.saveAndFlush(cart)

        val cartFormationInfo = cartFormationInfoRepository.findByCartId(cart.id!!)!!
        cartFormationInfoRepository.delete(cartFormationInfo)
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
        cartFormationInfo.deliveryDate = if (form.deliveryDate == null) cartFormationInfo.deliveryDate else form.deliveryDate!!

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
            paymentMethod = cartFormationInfo.paymentMethod.name,
            deliveryDate = cartFormationInfo.deliveryDate
        )
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CartService::class.java)
    }
}