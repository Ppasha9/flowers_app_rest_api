package com.flowersapp.flowersappserver.controllers

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.datatables.orders.OrderRepository
import com.flowersapp.flowersappserver.services.orders.OrderService
import com.flowersapp.flowersappserver.services.users.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/order")
class OrderController {
    @Autowired
    private lateinit var orderService: OrderService
    @Autowired
    private lateinit var userService: UserService
    @Autowired
    private lateinit var orderRepository: OrderRepository

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("")
    fun getOrdersForUser(): ResponseEntity<Any> {
        logger.debug("Getting all orders for current user")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        return ResponseEntity.ok(orderService.getOrdersCuttedForm(currUser))
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("/{id}")
    fun getOrderDetailsInfo(@PathVariable(name = "id", required = true) orderId: Long): ResponseEntity<Any> {
        logger.debug("Getting order details info for order with id=$orderId")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        if (!orderRepository.existsById(orderId)) {
            return ResponseEntity("Not found order with id=$orderId", HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity.ok(orderService.getOrderFullInfo(orderId))
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(OrderController::class.java)
    }
}