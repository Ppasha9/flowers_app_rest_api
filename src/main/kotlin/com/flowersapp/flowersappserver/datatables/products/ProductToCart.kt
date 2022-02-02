package com.flowersapp.flowersappserver.datatables.products

import com.flowersapp.flowersappserver.datatables.carts.Cart
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "products_parameters_for_products_in_carts")
data class ProductParametersForProductInCart(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_parameters_for_products_in_carts_gen")
    @SequenceGenerator(name = "products_parameters_for_products_in_carts_gen", sequenceName = "products_parameters_for_products_in_carts_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_to_cart_id")
    var productToCart: ProductToCart,

    @Column(name = "parameter_name")
    var parameterName: String? = null,
    @Column(name = "parameter_value")
    var parameterValue: String? = null,
    @Column(name = "parameter_price")
    var parameterPrice: Double? = null
)

interface ProductParametersForProductInCartRepository: JpaRepository<ProductParametersForProductInCart, Long> {
    fun existsByProductToCartId(productToCartId: Long): Boolean

    fun findByProductToCartId(productToCartId: Long): List<ProductParametersForProductInCart>
}

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
    fun findByCartIdAndProductId(cartId: Long, productId: Long): List<ProductToCart>
}
