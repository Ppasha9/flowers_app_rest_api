package com.flowersapp.flowersappserver.datatables.users

import com.flowersapp.flowersappserver.constants.Constants
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "user_types")
data class UserType(
    @Id
    @NotEmpty(message = "Please provide UserType code: `admin`, `casual` and etc.")
    @Column(unique = true, length = Constants.STRING_LENGTH_SHORT)
    var code: String = "",

    @NotEmpty(message = "Please provide current UserType description.")
    @Column(columnDefinition = "TEXT")
    var description: String = ""
)

interface UserTypeRepository : JpaRepository<UserType, String> {
    fun existsByCode(code: String): Boolean

    @Transactional
    fun findByCode(code: String): UserType?
}
