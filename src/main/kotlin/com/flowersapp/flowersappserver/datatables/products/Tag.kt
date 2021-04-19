package com.flowersapp.flowersappserver.datatables.products

import com.flowersapp.flowersappserver.constants.Constants
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "tags")
data class Tag(
    @Id
    @NotEmpty(message = "Please provide Tag code: `pink`, `red` and etc")
    @Column(unique = true, length = Constants.STRING_LENGTH_LONG)
    var code: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String = ""
)

interface TagRepository: JpaRepository<Tag, String> {
    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): Tag?
}