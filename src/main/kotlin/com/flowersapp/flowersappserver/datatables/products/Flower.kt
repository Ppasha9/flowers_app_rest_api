package com.flowersapp.flowersappserver.datatables.products

import com.flowersapp.flowersappserver.constants.Constants
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "flowers")
data class Flower(
    @Id
    @NotEmpty(message = "Please provide Flower code: `rose` and etc")
    @Column(unique = true, length = Constants.STRING_LENGTH_LONG)
    var code: String = ""
)

interface FlowerRepository: JpaRepository<Flower, String> {
    fun existsByCode(code: String): Boolean
    fun findByCode(code: String): Flower?
}