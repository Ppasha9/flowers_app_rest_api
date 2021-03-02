package com.flowersapp.flowersappserver

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.datatables.carts.CartStatus
import com.flowersapp.flowersappserver.datatables.carts.CartStatusRepository
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
        cartStatusRepository: CartStatusRepository,
        passwordEncoder: BCryptPasswordEncoder,
        productRepository: ProductRepository,
        productToCategoryRepository: ProductToCategoryRepository,
        orderStatusRepository: OrderStatusRepository
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

            if (!userRepository.existsByCode("admin_code")) {
                val adminUser = User(
                    userType = userTypeRepository.findByCode(Constants.ADMIN_USER_TYPE_CODE)!!,
                    name = "ADmin",
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
                Category(Constants.CATEGORY_BOUQUET_CODE, "Все возможные заранее собранные букеты")
            ).forEach {
                if (!categoryRepository.existsByCode(it.code)) {
                    categoryRepository.saveAndFlush(it)
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

            val pr1 = Product(
                name = "Весна в руке",
                description = "Самый нежный и трогательный букет с невероятным, голубым, как небо весной, оксипеталумом. \n" +
                    "Все наши букеты индивидуальны и сделаны, учитывая ваши пожелания. Мы оставляем за собой авторское право определять конечный состав букета, учитывая сезонность цветов, но обещаем, что букет останется таким же стильным и неповторимым.",
                price = 7000.0
            )
            productRepository.saveAndFlush(pr1)
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr1, category = categoryRepository.findByCode(Constants.CATEGORY_BOUQUET_CODE)!!))

            val pr2 = Product(
                name = "Смольный",
                description = "Нежный букет с уютных хлопком и пионовидными розами.Все наши букеты индивидуальны и сделаны, учитывая ваши пожелания. Мы оставляем за собой авторское право определять конечный состав букета, учитывая сезонность цветов, но обещаем, что букет останется таким же стильным и неповторимым.",
                price = 4700.0
            )
            productRepository.saveAndFlush(pr2)
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr2, category = categoryRepository.findByCode(Constants.CATEGORY_NEW_CODE)!!))
        }
    }
}

fun main(args: Array<String>) {
    runApplication<FlowersAppServerApplication>(*args)
}
