package com.flowersapp.flowersappserver.datatables.products

import com.flowersapp.flowersappserver.datatables.carts.Cart
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "products_to_carts")
data class ProductToCart(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_to_carts_gen")
    @SequenceGenerator(name = "products_to_carts_gen", sequenceName = "products_to_carts_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_code")
    var cart: Cart,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code")
    var product: Product,

    var amount: Int = 1
)

interface ProductToCartRepository: JpaRepository<ProductToCart, Long> {
    fun existsByCartId(cartId: Long): Boolean
    fun existsByProductId(productId: Long): Boolean
    fun existsByCartIdAndProductId(cartId: Long, productId: Long): Boolean

    fun findByCartId(cartId: Long): List<ProductToCart>
    fun findByProductId(productId: Long): List<ProductToCart>
    fun findByCartIdAndProductId(cartId: Long, productId: Long): ProductToCart?
}
