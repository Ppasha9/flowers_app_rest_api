package com.flowersapp.flowersappserver.datatables.users

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "users_to_delivery_addresses")
data class UserToDeliveryAddress(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_to_delivery_addresses_gen")
    @SequenceGenerator(name = "users_to_delivery_addresses_gen", sequenceName = "users_to_delivery_addresses_seq")
    var id: Long? = null,

    @Column(name = "user_code")
    var userCode: String,

    var addressName: String,
    var houseAddress: String,
    var flatNum: Int,
    var entranceNum: Int,
    var floorNum: Int,
    var comment: String? = null
)

interface UserToDeliveryAddressRepository: JpaRepository<UserToDeliveryAddress, Long> {
    fun existsByUserCode(userCode: String): Boolean
    fun existsByUserCodeAndAddressName(userCode: String, addressName: String): Boolean

    fun findByUserCode(userCode: String): List<UserToDeliveryAddress>
    fun findByUserCodeAndAddressName(userCode: String, addressName: String): UserToDeliveryAddress?
}