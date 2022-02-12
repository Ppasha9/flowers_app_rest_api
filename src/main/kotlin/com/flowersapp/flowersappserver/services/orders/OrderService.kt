package com.flowersapp.flowersappserver.services.orders

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.controllers.ProductController
import com.flowersapp.flowersappserver.datatables.carts.CartFormationInfoRepository
import com.flowersapp.flowersappserver.datatables.carts.DeliveryMethod
import com.flowersapp.flowersappserver.datatables.carts.PaymentMethod
import com.flowersapp.flowersappserver.datatables.orders.*
import com.flowersapp.flowersappserver.datatables.products.*
import com.flowersapp.flowersappserver.datatables.users.User
import com.flowersapp.flowersappserver.forms.carts.ProductParameterForm
import com.flowersapp.flowersappserver.forms.orders.*
import com.flowersapp.flowersappserver.forms.products.UploadOrderPictureForm
import com.flowersapp.flowersappserver.forms.products.UploadProductPictureForm
import com.flowersapp.flowersappserver.services.carts.CartService
import com.flowersapp.flowersappserver.services.pictures.PictureService
import com.flowersapp.flowersappserver.services.products.ProductService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder

@Service
class OrderService {
    @Autowired
    private lateinit var orderRepository: OrderRepository
    @Autowired
    private lateinit var orderStatusRepository: OrderStatusRepository
    @Autowired
    private lateinit var productToOrderRepository: ProductToOrderRepository
    @Autowired
    private lateinit var productRepository: ProductRepository
    @Autowired
    private lateinit var orderToPictureRepository: OrderToPictureRepository

    @Autowired
    private lateinit var orderCardRepository: OrderCardRepository
    @Autowired
    private lateinit var orderColumnRepository: OrderColumnRepository
    @Autowired
    private lateinit var orderCardToOrderColumnRepository: OrderCardToOrderColumnRepository

    @Autowired
    private lateinit var cartService: CartService
    @Autowired
    private lateinit var cartFormationInfoRepository: CartFormationInfoRepository
    @Autowired
    private lateinit var productToCartRepository: ProductToCartRepository
    @Autowired
    private lateinit var productParametersForProductInCartRepository: ProductParametersForProductInCartRepository
    @Autowired
    private lateinit var productParametersForProductInOrderRepository: ProductParametersForProductInOrderRepository

    @Autowired
    private lateinit var pictureService: PictureService

    @Transactional
    fun getAll(): List<Order> {
        return orderRepository.findAll()
    }

    @Transactional
    fun createOrderFromCart(user: User) {
        val cart = cartService.getCartForUser(user)
        val cartFormationInfo = cartFormationInfoRepository.findByCartId(cart.id!!)!!
        var order = Order(
            userCode = user.code,
            price = cart.price,
            status = orderStatusRepository.findByCode(Constants.ORDER_STATUS_FORMING)!!,
            paymentMethod = cartFormationInfo.paymentMethod,
            deliveryMethod = cartFormationInfo.deliveryMethod,
            deliveryComment = cartFormationInfo.deliveryComment,
            receiverStreet = cartFormationInfo.receiverStreet,
            receiverPhone = cartFormationInfo.receiverPhone,
            receiverHouseNum = cartFormationInfo.receiverHouseNum,
            receiverEmail = cartFormationInfo.receiverEmail,
            receiverApartmentNum = cartFormationInfo.receiverApartmentNum,
            receiverName = cartFormationInfo.receiverName,
            receiverSurname = cartFormationInfo.receiverSurname,
            deliveryDate = cartFormationInfo.deliveryDate
        )
        order = orderRepository.saveAndFlush(order)

        val orderCard = orderCardRepository.saveAndFlush(OrderCard(
            order = order
        ))
        val orderColumnName = "${order.deliveryDate!!.dayOfMonth}.${order.deliveryDate!!.monthValue}.${order.deliveryDate!!.year}"
        val orderColumn = if (!orderColumnRepository.existsByName(orderColumnName)) {
            orderColumnRepository.saveAndFlush(OrderColumn(name = orderColumnName))
        } else {
            orderColumnRepository.findByName(orderColumnName)
        }

        orderCardToOrderColumnRepository.saveAndFlush(OrderCardToOrderColumn(
            orderCard = orderCard,
            orderColumn = orderColumn!!
        ))

        productToCartRepository.findByCartId(cart.id!!).forEach {
            var prToOrder = ProductToOrder(
                product = it.product,
                order = order,
                amount = it.amount
            )
            prToOrder = productToOrderRepository.saveAndFlush(prToOrder)

            productParametersForProductInCartRepository.findByProductToCartId(it.id!!).forEach { paramsInCart ->
                productParametersForProductInOrderRepository.saveAndFlush(
                    ProductParametersForProductInOrder(
                        productToOrder = prToOrder,
                        parameterName = paramsInCart.parameterName,
                        parameterValue = paramsInCart.parameterValue,
                        parameterPrice = paramsInCart.parameterPrice
                    )
                )

                productParametersForProductInCartRepository.delete(paramsInCart)
            }

            productToCartRepository.delete(it)
        }
    }

    @Transactional
    fun createOrderFromAdminForm(form: OrderCreateAdminForm): String? {
        if (!orderColumnRepository.existsByName(form.columnName)) {
            orderColumnRepository.saveAndFlush(OrderColumn(name = form.columnName))
        }

        var order = Order(
            price = form.price,
            status = orderStatusRepository.findByCode(Constants.ORDER_STATUS_FORMING)!!,
            paymentMethod = PaymentMethod.fromString(form.paymentMethod),
            deliveryMethod = DeliveryMethod.fromString(form.deliveryMethod),
            deliveryComment = form.deliveryComment,
            receiverStreet = form.receiverStreet,
            receiverPhone = form.receiverPhone,
            receiverHouseNum = form.receiverHouseNum,
            receiverEmail = form.receiverEmail,
            receiverApartmentNum = form.receiverApartmentNum,
            receiverName = form.receiverName,
            receiverSurname = form.receiverSurname,
            shortDescription = form.shortDescription,
            productsDescription = form.productsDescription,
            deliveryDate = form.deliveryDate
        )

        order = orderRepository.saveAndFlush(order)
        val orderCard = orderCardRepository.saveAndFlush(OrderCard(order = order))
        orderCardToOrderColumnRepository.saveAndFlush(
            OrderCardToOrderColumn(
                orderCard = orderCard,
                orderColumn = orderColumnRepository.findByName(form.columnName)!!
            )
        )

        form.selectedProductsDescrs.forEach {
            if (!productRepository.existsById(it.id)) {
                return "There isn't product with id ${it.id}"
            }

            val productToOrder = productToOrderRepository.saveAndFlush(ProductToOrder(
                order = order,
                product = productRepository.findById(it.id).get()
            ))
            it.parameters.forEach { selectedParam ->
                productParametersForProductInOrderRepository.save(
                    ProductParametersForProductInOrder(
                        parameterName = selectedParam.name,
                        parameterValue = selectedParam.value,
                        parameterPrice = selectedParam.price,
                        productToOrder = productToOrder
                    )
                )
            }
        }

        return null
    }

    @Transactional
    fun getOrdersCuttedForm(user: User): OrdersForm {
        val res = OrdersForm(orders = arrayListOf())

        orderRepository.findByUserCode(user.code).forEach { order ->
            val ocf = OrderCuttedForm(
                id = order.id!!,
                fullPrice = if (order.deliveryMethod == DeliveryMethod.PICKUP) order.price else order.price + 300.0,
                status = order.status.code,
                deliveryMethod = order.deliveryMethod.name,
                products = arrayListOf()
            )

            productToOrderRepository.findByOrderId(order.id!!).forEach {
                val toAdd = ProductInOrderForm(
                    id = it.product.id!!,
                    name = it.product.name,
                    price = it.product.price,
                    amount = it.amount,
                    parameters = arrayListOf()
                )

                productParametersForProductInOrderRepository.findByProductToOrderId(it.id!!).forEach { param ->
                    toAdd.parameters.add(ProductParameterForm(
                        parameterPrice = param.parameterPrice,
                        parameterValue = param.parameterValue,
                        parameterName = param.parameterName
                    ))
                }

                ocf.products.add(toAdd)
            }

            res.orders.add(ocf)
        }

        return res
    }

    @Transactional
    fun getOrderFullInfo(orderId: Long): OrderFullForm {
        val order = orderRepository.findById(orderId).get()
        val res = OrderFullForm(
            id = order.id!!,
            price = order.price,
            fullPrice = if (order.deliveryMethod == DeliveryMethod.PICKUP) order.price else order.price + 300.0,
            status = order.status.code,
            receiverName = order.receiverName,
            receiverSurname = order.receiverSurname,
            receiverApartmentNum = order.receiverApartmentNum,
            receiverEmail = order.receiverEmail,
            receiverHouseNum = order.receiverHouseNum,
            receiverPhone = order.receiverPhone,
            receiverStreet = order.receiverStreet,
            deliveryMethod = order.deliveryMethod.name,
            deliveryComment = order.deliveryComment,
            paymentMethod = order.paymentMethod.name,
            shortDescription = order.shortDescription,
            productsDescription = order.productsDescription,
            deliveryDate = order.deliveryDate,
            products = arrayListOf()
        )

        productToOrderRepository.findByOrderId(order.id!!).forEach {
            val toAdd = ProductInOrderCuttedForm(
                id = it.product.id!!,
                amount = it.amount,
                name = it.product.name,
                parameters = arrayListOf()
            )

            productParametersForProductInOrderRepository.findByProductToOrderId(it.id!!).forEach { param ->
                toAdd.parameters.add(ProductParameterForm(
                    parameterPrice = param.parameterPrice,
                    parameterValue = param.parameterValue,
                    parameterName = param.parameterName
                ))
            }

            res.products.add(toAdd)
        }

        return res
    }

    @Transactional
    fun getOrderFullAdminForm(orderId: Long): OrderFullAdminForm {
        val res = OrderFullAdminForm(
            id = orderId,
            body = getOrderFullInfo(orderId),
            picFilename = "",
            picUrl = ""
        )

        if (!pictureService.canGetOrderPictures(orderId)) {
            return res
        }

        val loadFiles = pictureService.loadFiles()
        loadFiles.forEach {
            if (orderToPictureRepository.existsByOrderIdAndFilename(
                    orderId = orderId,
                    filename = it.fileName.toString()
                )) {
                res.picFilename = it.fileName.toString()
                res.picUrl = MvcUriComponentsBuilder.fromMethodName(
                    ProductController::class.java,
                    "downloadFile",
                    it.fileName.toString()
                ).build().toString()
            }
        }
        return res
    }

    @Transactional
    fun createOneClickOrder(user: User?, oneClickForm: OrderOneClickForm): String? {
        if (!productRepository.existsById(oneClickForm.productId)) {
            return "Product with id ${oneClickForm.productId} doesn't exist"
        }

        val product = productRepository.findById(oneClickForm.productId).get()
        var orderPrice = product.price
        for (param in oneClickForm.parameters ?: arrayListOf()) {
            if (param.parameterPrice != null) {
                orderPrice += param.parameterPrice!!
            }
        }

        var order = Order(
            userCode = user?.code,
            price = orderPrice,
            status = orderStatusRepository.findByCode(Constants.ORDER_STATUS_FORMING)!!,
            receiverName = oneClickForm.receiverName,
            receiverSurname = oneClickForm.receiverSurname,
            receiverPhone = oneClickForm.receiverPhone,
            receiverEmail = oneClickForm.receiverEmail,
            receiverStreet = oneClickForm.receiverStreet,
            receiverHouseNum = oneClickForm.receiverHouseNum,
            receiverApartmentNum = oneClickForm.receiverApartmentNum,
            deliveryComment = oneClickForm.deliveryComment,
            deliveryDate = oneClickForm.deliveryDate,
            deliveryMethod = DeliveryMethod.fromString(oneClickForm.deliveryMethod),
            paymentMethod = PaymentMethod.fromString(oneClickForm.paymentMethod)
        )
        order = orderRepository.saveAndFlush(order)

        val orderCard = orderCardRepository.saveAndFlush(OrderCard(
            order = order
        ))
        val orderColumnName = "${order.deliveryDate!!.dayOfMonth}.${order.deliveryDate!!.monthValue}.${order.deliveryDate!!.year}"
        val orderColumn = if (!orderColumnRepository.existsByName(orderColumnName)) {
             orderColumnRepository.saveAndFlush(OrderColumn(name = orderColumnName))
        } else {
            orderColumnRepository.findByName(orderColumnName)
        }
        orderCardToOrderColumnRepository.saveAndFlush(OrderCardToOrderColumn(
            orderCard = orderCard,
            orderColumn = orderColumn!!
        ))

        var prToOrder = ProductToOrder(
            product = product,
            order = order,
            amount = 1
        )
        prToOrder = productToOrderRepository.saveAndFlush(prToOrder)
        for (param in oneClickForm.parameters ?: arrayListOf()) {
            productParametersForProductInOrderRepository.saveAndFlush(
                ProductParametersForProductInOrder(
                    productToOrder = prToOrder,
                    parameterName = param.parameterName,
                    parameterValue = param.parameterValue,
                    parameterPrice = param.parameterPrice
                )
            )
        }
        return null
    }

    @Transactional
    fun createOrderColumn(columnName: String): String? {
        if (orderColumnRepository.existsByName(columnName)) {
            return "Order column with name $columnName already exists"
        }

        val orderColumn = OrderColumn(name = columnName)
        orderColumnRepository.saveAndFlush(orderColumn)
        return null
    }

    @Transactional
    fun getAllCardsWithColumns(): OrderCardsWithColumnsForm {
        val orderCards = orderCardRepository.findAll()
        val orderColumns = orderColumnRepository.findAll()

        val res = OrderCardsWithColumnsForm(
            cards = arrayListOf(),
            columns = orderColumns.map { column -> column.name } as ArrayList<String>
        )

        orderCards.forEach {
            val columnName = orderCardToOrderColumnRepository.findByOrderCardId(it.id!!)!!.orderColumn.name
            res.cards.add(
                OrderCardForm(
                    order = getOrderFullAdminForm(it.order.id!!),
                    columnName = columnName
                )
            )
        }

        return res
    }
}