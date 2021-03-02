package com.flowersapp.flowersappserver.datatables.products

import com.flowersapp.flowersappserver.constants.Constants
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "categories")
data class Category(
    @Id
    @NotEmpty(message = "Please provide Category code: `new`, `bouquet` and etc")
    @Column(unique = true, length = Constants.STRING_LENGTH_SHORT)
    var code: String = "",

    @NotEmpty(message = "Please provide current Category description.")
    @Column(columnDefinition = "TEXT")
    var description: String = ""
)

interface CategoryRepository: JpaRepository<Category, String> {
    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): Category?
}