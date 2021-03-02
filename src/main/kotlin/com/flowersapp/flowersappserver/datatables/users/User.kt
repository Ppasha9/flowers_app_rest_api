package com.flowersapp.flowersappserver.datatables.users

import com.flowersapp.flowersappserver.constants.Constants
import org.hibernate.annotations.GenericGenerator
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(unique = true, length = Constants.STRING_LENGTH_MIDDLE)
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var code: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_type_code")
    @NotEmpty(message = "Please provide user's type")
    var userType: UserType,

    @NotEmpty(message = "Please provide user's name")
    var name: String = "",

    @Email
    @NotEmpty(message = "Please provide user's email")
    var email: String = "",

    var password: String = "",

    var phone: String = "",

    @Column(name = "external_code", length = Constants.STRING_LENGTH_MIDDLE)
    var externalCode: String = "",

    var isOAuth: Boolean = false
)

fun User.toHashMap(): HashMap<String, Any> {
    val res = HashMap<String, Any>()
    res["id"] = code
    res["userType"] = userType.toString()
    res["email"] = email
    res["name"] = name
    res["phone"] = phone.toString()
    res["isOAuth"] = isOAuth.toString()

    return res
}

interface UserRepository : JpaRepository<User, String> {
    fun existsByEmail(email: String): Boolean
    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): User?
    fun findByEmail(email: String): User?
}