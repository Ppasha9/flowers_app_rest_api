package com.flowersapp.flowersappserver.datatables.carts

import com.flowersapp.flowersappserver.constants.Constants
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "cart_statuses")
data class CartStatus(
    @Id
    @NotEmpty(message = "Please provide CartStatus code: `forming`, `cancelled` and 'succeeded'")
    @Column(unique = true, length = Constants.STRING_LENGTH_SHORT)
    var code: String = "",

    @NotEmpty(message = "Please provide current CartStatus description.")
    @Column(columnDefinition = "TEXT")
    var description: String = ""
)

interface CartStatusRepository: JpaRepository<CartStatus, String> {
    fun existsByCode(code: String): Boolean

    @Transactional
    fun findByCode(code: String): CartStatus?
}