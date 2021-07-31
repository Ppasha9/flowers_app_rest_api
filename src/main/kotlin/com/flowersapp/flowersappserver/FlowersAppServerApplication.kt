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
        tagRepository: TagRepository,
        cartStatusRepository: CartStatusRepository,
        passwordEncoder: BCryptPasswordEncoder,
        productRepository: ProductRepository,
        productToCategoryRepository: ProductToCategoryRepository,
        productToTagRepository: ProductToTagRepository,
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
                Tag(Constants.TAG_ROSES_CODE),
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

            /*
            val pr1 = Product(
                name = "Весна в руке",
                description = "Самый нежный и трогательный букет с невероятным, голубым, как небо весной, оксипеталумом. \n" +
                    "Все наши букеты индивидуальны и сделаны, учитывая ваши пожелания. Мы оставляем за собой авторское право определять конечный состав букета, учитывая сезонность цветов, но обещаем, что букет останется таким же стильным и неповторимым.",
                price = 7000.0
            )
            productRepository.saveAndFlush(pr1)
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr1, category = categoryRepository.findByCode(Constants.CATEGORY_BOUQUET_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr1, tag = tagRepository.findByCode(Constants.TAG_SOFT_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr1, tag = tagRepository.findByCode(Constants.TAG_BIRTHDAY_CODE)!!))

            val pr2 = Product(
                name = "Смольный",
                description = "Нежный букет с уютных хлопком и пионовидными розами.Все наши букеты индивидуальны и сделаны, учитывая ваши пожелания. Мы оставляем за собой авторское право определять конечный состав букета, учитывая сезонность цветов, но обещаем, что букет останется таким же стильным и неповторимым.",
                price = 4700.0
            )
            productRepository.saveAndFlush(pr2)
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr2, category = categoryRepository.findByCode(Constants.CATEGORY_BOUQUET_CODE)!!))
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr2, category = categoryRepository.findByCode(Constants.CATEGORY_NEW_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr2, tag = tagRepository.findByCode(Constants.TAG_BLUE_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr2, tag = tagRepository.findByCode(Constants.TAG_FREE_SHIPPMENT_CODE)!!))

            val pr3 = Product(
                name = "Корзина розовый рассвет",
                description = "Пышная корзина с пионовидными розами, антуриумом и гортензией. Мы оставляем за собой авторское право определять конечный состав букета, учитывая сезонность цветов, но обещаем, что букет останется таким же стильным и неповторимым.",
                price = 14500.0
            )
            productRepository.saveAndFlush(pr3)
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr3, category = categoryRepository.findByCode(Constants.CATEGORY_BASKET_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr3, tag = tagRepository.findByCode(Constants.TAG_PINK_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr3, tag = tagRepository.findByCode(Constants.TAG_ROSES_CODE)!!))

            val pr4 = Product(
                name = "Корзина розовая мечта",
                description = "Нежная корзина с добавлением хлопка, пионовидных роз, маттиолы и лизиантусов",
                price = 10000.0
            )
            productRepository.saveAndFlush(pr4)
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr4, category = categoryRepository.findByCode(Constants.CATEGORY_BASKET_CODE)!!))
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr4, category = categoryRepository.findByCode(Constants.CATEGORY_NEW_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr4, tag = tagRepository.findByCode(Constants.TAG_SIMPLY_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr4, tag = tagRepository.findByCode(Constants.TAG_TO_GIRLFRIEND_CODE)!!))

            val pr5 = Product(
                name = "Счастье",
                description = "Пышный букет с пионовидными розами и ароматной маттиолой",
                price = 7200.0
            )
            productRepository.saveAndFlush(pr5)
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr5, category = categoryRepository.findByCode(Constants.CATEGORY_BOUQUET_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr5, tag = tagRepository.findByCode(Constants.TAG_TO_LOVE_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr5, tag = tagRepository.findByCode(Constants.TAG_TO_MOM_CODE)!!))

            val pr6 = Product(
                name = "Яркое омбре",
                description = "Яркий букет с красивым пеходом от белого к красному. В составе всеми любимые пионовидные розы и много другой красоты.Все наши букеты индивидуальны и сделаны, учитывая ваши пожелания. Мы оставляем за собой авторское право определять конечный состав букета, учитывая сезонность цветов, но обещаем, что букет останется таким же стильным и неповторимым.",
                price = 6500.0
            )
            productRepository.saveAndFlush(pr6)
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr6, category = categoryRepository.findByCode(Constants.CATEGORY_BOUQUET_CODE)!!))
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr6, category = categoryRepository.findByCode(Constants.CATEGORY_NEW_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr6, tag = tagRepository.findByCode(Constants.TAG_SOFT_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr6, tag = tagRepository.findByCode(Constants.TAG_BIRTHDAY_CODE)!!))

            val pr7 = Product(
                name = "Большие чувства",
                description = "Огромный букет с красными розами для самых больший чувств. Такой букет точно попадет в самое сердце.",
                price = 18000.0
            )
            productRepository.saveAndFlush(pr7)
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr7, category = categoryRepository.findByCode(Constants.CATEGORY_BOUQUET_CODE)!!))
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr7, category = categoryRepository.findByCode(Constants.CATEGORY_NEW_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr7, tag = tagRepository.findByCode(Constants.TAG_FREE_SHIPPMENT_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr7, tag = tagRepository.findByCode(Constants.TAG_PINK_CODE)!!))

            val pr8 = Product(
                name = "Корзина счастья",
                description = "Корзина с пионовидными розами джульетта",
                price = 7000.0
            )
            productRepository.saveAndFlush(pr8)
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr8, category = categoryRepository.findByCode(Constants.CATEGORY_BASKET_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr8, tag = tagRepository.findByCode(Constants.TAG_SIMPLY_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr8, tag = tagRepository.findByCode(Constants.TAG_TO_MOM_CODE)!!))

            val pr9 = Product(
                name = "Композиция розовое счастье",
                description = "Большая шляпная коробка с самыми нежныии цветами.  Мы оставляем за собой авторское право определять конечный состав букета, учитывая сезонность цветов, но обещаем, что букет останется таким же стильным и неповторимым.",
                price = 8500.0
            )
            productRepository.saveAndFlush(pr9)
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr9, category = categoryRepository.findByCode(Constants.CATEGORY_BASKET_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr9, tag = tagRepository.findByCode(Constants.TAG_SOFT_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr9, tag = tagRepository.findByCode(Constants.TAG_TO_LOVE_CODE)!!))

            val pr10 = Product(
                name = "Весенняя корзина",
                description = "Большая корзина, которая принесет настоящее весеннее настроение. Более 15 сортов всевозможной цветочной красоты.",
                price = 18000.0
            )
            productRepository.saveAndFlush(pr10)
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr10, category = categoryRepository.findByCode(Constants.CATEGORY_BASKET_CODE)!!))
            productToCategoryRepository.saveAndFlush(ProductToCategory(product = pr10, category = categoryRepository.findByCode(Constants.CATEGORY_NEW_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr10, tag = tagRepository.findByCode(Constants.TAG_TO_LOVE_CODE)!!))
            productToTagRepository.saveAndFlush(ProductToTag(product = pr10, tag = tagRepository.findByCode(Constants.TAG_TO_MOM_CODE)!!))
             */
        }
    }
}

fun main(args: Array<String>) {
    runApplication<FlowersAppServerApplication>(*args)
}
