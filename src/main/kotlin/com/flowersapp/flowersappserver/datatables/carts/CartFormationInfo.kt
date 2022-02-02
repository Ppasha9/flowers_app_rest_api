package com.flowersapp.flowersappserver.datatables.carts

import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import javax.persistence.*

enum class DeliveryMethod {
    NONE,
    COURIER,
    PICKUP;

    companion object {
        fun fromString(str: String): DeliveryMethod {
            when (str) {
                "courier" -> return COURIER
                "pickup" -> return PICKUP
            }
            return NONE
        }
    }
}

enum class PaymentMethod {
    NONE,
    ONLINE,
    CASH;

    companion object {
        fun fromString(str: String): PaymentMethod {
            when (str) {
                "online" -> return ONLINE
                "cash" -> return CASH
            }
            return NONE
        }
    }
}

@Entity
@Table(name = "carts_formation_info")
data class CartFormationInfo(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "carts_formation_info_gen")
    @SequenceGenerator(name = "carts_formation_info_gen", sequenceName = "carts_formation_info_seq")
    var id: Long? = null,

    @Column(name = "cart_id")
    var cartId: Long = 0,

    @Column(name = "receiver_name")
    var receiverName: String = "",

    @Column(name = "receiver_surname")
    var receiverSurname: String = "",

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

    @Column(name = "delivery_date")
    var deliveryDate: OffsetDateTime? = null,

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    var paymentMethod: PaymentMethod = PaymentMethod.CASH
)

interface CartFormationInfoRepository: JpaRepository<CartFormationInfo, Long> {
    fun existsByCartId(cartId: Long): Boolean

    fun findByCartId(cartId: Long): CartFormationInfo?
}
