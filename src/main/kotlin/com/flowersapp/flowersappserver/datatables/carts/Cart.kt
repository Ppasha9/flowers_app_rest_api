package com.flowersapp.flowersappserver.datatables.carts

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "carts")
data class Cart(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "carts_gen")
    @SequenceGenerator(name = "carts_gen", sequenceName = "carts_seq")
    var id: Long? = null,

    @Column(name = "user_code")
    var userCode: String? = null,

    var price: Double = 0.0,

    @ManyToOne
    @JoinColumn(name = "cart_status_code")
    var status: CartStatus
)

interface CartRepository: JpaRepository<Cart, Long> {
    fun existsByUserCode(userCode: String): Boolean

    fun findByUserCode(userCode: String): Cart?
}