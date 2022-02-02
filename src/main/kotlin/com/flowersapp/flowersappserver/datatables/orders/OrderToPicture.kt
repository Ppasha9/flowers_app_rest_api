package com.flowersapp.flowersappserver.datatables.orders

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "orders_to_pictures")
data class OrderToPicture(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_to_pictures_gen")
    @SequenceGenerator(name = "orders_to_pictures_gen", sequenceName = "orders_to_pictures_seq")
    var id: Long? = null,

    @NotBlank
    var filename: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    var order: Order
)

interface OrderToPictureRepository: JpaRepository<OrderToPicture, Long> {
    fun existsByOrderId(orderId: Long): Boolean
    fun existsByOrderIdAndFilename(orderId: Long, filename: String): Boolean

    fun findByOrderId(orderId: Long): List<OrderToPicture>
    fun findByOrderIdAndFilename(orderId: Long, filename: String): OrderToPicture?
}
