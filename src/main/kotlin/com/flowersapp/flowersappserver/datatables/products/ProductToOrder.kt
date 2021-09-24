package com.flowersapp.flowersappserver.datatables.products

import com.flowersapp.flowersappserver.datatables.orders.Order
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "products_to_orders")
data class ProductToOrder(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_to_orders_gen")
    @SequenceGenerator(name = "products_to_orders_gen", sequenceName = "products_to_orders_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_code")
    var order: Order,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code")
    var product: Product,

    var amount: Int = 1,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id")
    var productParameter: ProductParameter? = null
)

interface ProductToOrderRepository: JpaRepository<ProductToOrder, Long> {
    fun existsByOrderId(orderId: Long): Boolean
    fun existsByProductId(productId: Long): Boolean
    fun existsByOrderIdAndProductId(orderId: Long, productId: Long): Boolean

    fun findByOrderId(orderId: Long): List<ProductToOrder>
    fun findByProductId(productId: Long): List<ProductToOrder>
    fun findByOrderIdAndProductId(orderId: Long, productId: Long): ProductToOrder?
}