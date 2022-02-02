package com.flowersapp.flowersappserver

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.datatables.carts.CartStatus
import com.flowersapp.flowersappserver.datatables.carts.CartStatusRepository
import com.flowersapp.flowersappserver.datatables.orders.OrderColumn
import com.flowersapp.flowersappserver.datatables.orders.OrderColumnRepository
import com.flowersapp.flowersappserver.datatables.orders.OrderStatus
import com.flowersapp.flowersappserver.datatables.orders.OrderStatusRepository
import com.flowersapp.flowersappserver.datatables.products.*
import com.flowersapp.flowersappserver.datatables.users.User
import com.flowersapp.flowersappserver.datatables.users.UserRepository
import com.flowersapp.flowersappserver.datatables.users.UserType
import com.flowersapp.flowersappserver.datatables.users.UserTypeRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@SpringBootApplication
class FlowersAppServerApplication {
    @Bean
    internal fun initDatabase(
        userTypeRepository: UserTypeRepository,
        userRepository: UserRepository,
        categoryRepository: CategoryRepository,
        tagRepository: TagRepository,
        flowerRepository: FlowerRepository,
        cartStatusRepository: CartStatusRepository,
        passwordEncoder: BCryptPasswordEncoder,
        productRepository: ProductRepository,
        productToCategoryRepository: ProductToCategoryRepository,
        productToTagRepository: ProductToTagRepository,
        orderStatusRepository: OrderStatusRepository,
        orderColumnRepository: OrderColumnRepository
    ): CommandLineRunner {
        return CommandLineRunner { _ ->
            listOf(
                UserType(Constants.DEFAULT_USER_TYPE_CODE, "Обычный пользователь"),
                UserType(Constants.ADMIN_USER_TYPE_CODE, "Администратор")
            ).forEach {
                if (!userTypeRepository.existsByCode(it.code)) {
                    userTypeRepository.saveAndFlush(it)
                }
            }

            if (!userRepository.existsByEmail("admin@gmail.com")) {
                val adminUser = User(
                    userType = userTypeRepository.findByCode(Constants.ADMIN_USER_TYPE_CODE)!!,
                    name = "ADmin",
                    surname = "ADMIN_Surname",
                    password = passwordEncoder.encode("admin3059"),
                    phone = "3059",
                    email = "admin@gmail.com",
                    isOAuth = false,
                    code = "admin_code"
                )
                userRepository.saveAndFlush(adminUser)
            }

            listOf(
                Category(Constants.CATEGORY_NEW_CODE, "Новые товары в магазине"),
                Category(Constants.CATEGORY_BOUQUET_CODE, "Все возможные заранее собранные букеты"),
                Category(Constants.CATEGORY_BASKET_CODE, "Корзинки, собранные из самых красивых цветочков")
            ).forEach {
                if (!categoryRepository.existsByCode(it.code)) {
                    categoryRepository.saveAndFlush(it)
                }
            }

            listOf(
                Tag(Constants.TAG_SOFT_CODE),
                Tag(Constants.TAG_BIRTHDAY_CODE),
                Tag(Constants.TAG_TO_LOVE_CODE),
                Tag(Constants.TAG_TO_MOM_CODE),
                Tag(Constants.TAG_SIMPLY_CODE),
                Tag(Constants.TAG_PINK_CODE),
                Tag(Constants.TAG_BLUE_CODE),
                Tag(Constants.TAG_TO_GIRLFRIEND_CODE),
                Tag(Constants.TAG_FREE_SHIPPMENT_CODE)
            ).forEach {
                if (!tagRepository.existsByCode(it.code)) {
                    tagRepository.saveAndFlush(it)
                }
            }

            listOf(
                Flower(Constants.FLOWER_ROSE_CODE),
                Flower(Constants.FLOWER_TULIP_CODE),
                Flower(Constants.FLOWER_CHRYSANTHEMUMS_CODE)
            ).forEach {
                if (!flowerRepository.existsByCode(it.code)) {
                    flowerRepository.saveAndFlush(it)
                }
            }

            listOf(
                CartStatus(Constants.CART_STATUS_DEFAULT, "Пользователь только собирает корзину, еще не приступал к оформлению заказа"),
                CartStatus(Constants.CART_STATUS_RECEIVER_FORMATION, "Оформление заказа: указание данных о получателе"),
                CartStatus(Constants.CART_STATUS_SHIPPING_FORMATION, "Оформление заказа: указание способа и адреса доставки"),
                CartStatus(Constants.CART_STATUS_PAYMENT_FORMATION, "Оформление заказа: указание способа оплаты заказа")
            ).forEach {
                if (!cartStatusRepository.existsByCode(it.code)) {
                    cartStatusRepository.saveAndFlush(it)
                }
            }

            listOf(
                OrderStatus(Constants.ORDER_STATUS_FORMING, "Заказ собирается и готовится к доставке"),
                OrderStatus(Constants.ORDER_STATUS_SHIPPING, "Заказ доставляется"),
                OrderStatus(Constants.ORDER_STATUS_SUCCEEDED, "Заказ доставлен"),
                OrderStatus(Constants.ORDER_STATUS_CANCELLED, "Заказ отменен")
            ).forEach {
                if (!orderStatusRepository.existsByCode(it.code)) {
                    orderStatusRepository.saveAndFlush(it)
                }
            }

            if (!orderColumnRepository.existsByName("Backlog")) {
                orderColumnRepository.saveAndFlush(OrderColumn(name = "Backlog"))
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<FlowersAppServerApplication>(*args)
}
