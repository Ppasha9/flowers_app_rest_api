package com.flowersapp.flowersappserver.datatables.orders

import com.flowersapp.flowersappserver.constants.Constants
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "order_statuses")
data class OrderStatus(
    @Id
    @NotEmpty(message = "Please provide OrderStatus code: `forming`, `cancelled` and 'succeeded'")
    @Column(unique = true, length = Constants.STRING_LENGTH_SHORT)
    var code: String = "",

    @NotEmpty(message = "Please provide current OrderStatus description.")
    @Column(columnDefinition = "TEXT")
    var description: String = ""
)

interface OrderStatusRepository: JpaRepository<OrderStatus, String> {
    fun existsByCode(code: String): Boolean

    @Transactional
    fun findByCode(code: String): OrderStatus?
}