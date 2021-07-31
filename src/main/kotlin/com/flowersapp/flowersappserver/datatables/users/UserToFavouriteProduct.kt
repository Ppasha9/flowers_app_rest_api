package com.flowersapp.flowersappserver.datatables.users

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "users_to_favorite_products")
data class UserToFavouriteProduct(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_to_favorite_products_gen")
    @SequenceGenerator(name = "users_to_favorite_products_gen", sequenceName = "users_to_favorite_products_seq")
    var id: Long? = null,

    @Column(name = "user_code")
    var userCode: String? = null,

    @Column(name = "product_id")
    var productId: Long? = null
)

interface UserToFavouriteProductRepository: JpaRepository<UserToFavouriteProduct, Long> {
    fun existsByUserCode(userCode: String): Boolean
    fun existsByProductId(productId: Long): Boolean
    fun existsByUserCodeAndProductId(userCode: String, productId: Long): Boolean

    fun findByUserCode(userCode: String): List<UserToFavouriteProduct>
    fun findByProductId(productId: Long): List<UserToFavouriteProduct>
    fun findByUserCodeAndProductId(userCode: String, productId: Long): UserToFavouriteProduct?
}