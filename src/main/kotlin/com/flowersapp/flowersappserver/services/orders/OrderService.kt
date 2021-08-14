package com.flowersapp.flowersappserver.services.orders

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.datatables.carts.CartFormationInfoRepository
import com.flowersapp.flowersappserver.datatables.carts.DeliveryMethod
import com.flowersapp.flowersappserver.datatables.carts.PaymentMethod
import com.flowersapp.flowersappserver.datatables.orders.Order
import com.flowersapp.flowersappserver.datatables.orders.OrderRepository
import com.flowersapp.flowersappserver.datatables.orders.OrderStatus
import com.flowersapp.flowersappserver.datatables.orders.OrderStatusRepository
import com.flowersapp.flowersappserver.datatables.products.ProductRepository
import com.flowersapp.flowersappserver.datatables.products.ProductToCartRepository
import com.flowersapp.flowersappserver.datatables.products.ProductToOrder
import com.flowersapp.flowersappserver.datatables.products.ProductToOrderRepository
import com.flowersapp.flowersappserver.datatables.users.User
import com.flowersapp.flowersappserver.forms.orders.*
import com.flowersapp.flowersappserver.services.carts.CartService
import com.flowersapp.flowersappserver.services.products.ProductService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
    private lateinit var cartService: CartService
    @Autowired
    private lateinit var cartFormationInfoRepository: CartFormationInfoRepository
    @Autowired
    private lateinit var productToCartRepository: ProductToCartRepository

    @Transactional
    fun createOrderFromCart(user: User) {
        val cart = cartService.getCartForUser(user)
        val cartFormationInfo = cartFormationInfoRepository.findByCartId(cart.id!!)!!
        val order = Order(
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
            receiverSurname = cartFormationInfo.receiverSurname
        )

        orderRepository.saveAndFlush(order)

        productToCartRepository.findByCartId(cart.id!!).forEach {
            val prToOrder = ProductToOrder(
                product = it.product,
                order = order,
                amount = it.amount
            )
            productToOrderRepository.saveAndFlush(prToOrder)
            productToCartRepository.delete(it)
        }
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
                ocf.products.add(ProductInOrderForm(
                    id = it.product.id!!,
                    name = it.product.name,
                    price = it.product.price,
                    amount = it.amount
                ))
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
            products = arrayListOf()
        )

        productToOrderRepository.findByOrderId(order.id!!).forEach {
            res.products.add(ProductInOrderCuttedForm(id = it.product.id!!, amount = it.amount))
        }

        return res
    }

    @Transactional
    fun createOneClickOrder(user: User?, productId: Long, oneClickForm: OrderOneClickForm): String? {
        if (!productRepository.existsById(productId)) {
            return "Product with id $productId doesn't exist"
        }

        val order = Order(
            userCode = user?.code,
            price = productRepository.findById(productId).get().price,
            status = orderStatusRepository.findByCode(Constants.ORDER_STATUS_FORMING)!!,
            receiverName = oneClickForm.receiverName,
            receiverSurname = oneClickForm.receiverSurname,
            receiverPhone = oneClickForm.receiverPhone,
            receiverEmail = oneClickForm.receiverEmail,
            receiverStreet = oneClickForm.receiverStreet,
            receiverHouseNum = oneClickForm.receiverHouseNum,
            receiverApartmentNum = oneClickForm.receiverApartmentNum,
            deliveryComment = oneClickForm.deliveryComment,
            deliveryMethod = DeliveryMethod.fromString(oneClickForm.deliveryMethod),
            paymentMethod = PaymentMethod.fromString(oneClickForm.paymentMethod)
        )
        orderRepository.saveAndFlush(order)
        return null
    }
}