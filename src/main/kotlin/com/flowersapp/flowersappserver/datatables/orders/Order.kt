package com.flowersapp.flowersappserver.datatables.orders

import com.flowersapp.flowersappserver.datatables.carts.DeliveryMethod
import com.flowersapp.flowersappserver.datatables.carts.PaymentMethod
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_gen")
    @SequenceGenerator(name = "orders_gen", sequenceName = "orders_seq")
    var id: Long? = null,

    @Column(name = "user_code")
    var userCode: String? = null,

    var price: Double = 0.0,

    @ManyToOne
    @JoinColumn(name = "order_status_code")
    var status: OrderStatus,

    @Column(name = "receiver_name")
    var receiverName: String = "",

    @Column(name = "receiver_phone")
    var receiverPhone: String = "",

    @Column(name = "receiver_email")
    var receiverEmail: String = "",

    @Column(name = "receiver_street")
    var receiverStreet: String = "",

    @Column(name = "receiver_house_num")
    var receiverHouseNum: String = "",

    @Column(name = "receiver_apartment_num")
    var receiverApartmentNum: String = "",

    @Column(name = "delivery_comment")
    var deliveryComment: String = "",

    @Column(name = "delivery_method")
    @Enumerated(EnumType.STRING)
    var deliveryMethod: DeliveryMethod = DeliveryMethod.COURIER,

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    var paymentMethod: PaymentMethod = PaymentMethod.CASH
)

interface OrderRepository: JpaRepository<Order, Long> {
    fun existsByUserCode(userCode: String): Boolean

    fun findByUserCode(userCode: String): List<Order>
}
