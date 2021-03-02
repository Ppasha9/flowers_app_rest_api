package com.flowersapp.flowersappserver.services.users

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.datatables.users.User
import com.flowersapp.flowersappserver.datatables.users.UserRepository
import com.flowersapp.flowersappserver.datatables.users.UserTypeRepository
import com.flowersapp.flowersappserver.forms.authorization.MeUserForm
import com.flowersapp.flowersappserver.forms.authorization.SignUpUserForm
import com.flowersapp.flowersappserver.services.carts.CartService
import com.flowersapp.flowersappserver.utils.generateRandStr
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService {
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var userTypeRepository: UserTypeRepository
    @Autowired
    private lateinit var cartService: CartService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Transactional
    fun findByEmail(email: String): User? = userRepository.findByEmail(email)

    fun save(user: User) = userRepository.save(user)

    fun getCurrentAuthorizedUser(): User? = findByEmail(SecurityContextHolder.getContext().authentication.name)

    fun generateRandPassword(passwordLength: Int = Constants.STRING_LENGTH_SHORT) = generateRandStr()

    @Transactional
    fun signUp(signUpUserForm: SignUpUserForm): String {
        if (signUpUserForm.email.isBlank())
            return "Failed to execute sign up. Field `email` is required, but it is empty in this request."

        if (!signUpUserForm.email.isBlank() && userRepository.existsByEmail(signUpUserForm.email))
            return "Failed to execute sign up. There is already a user with email ${signUpUserForm.email}"

        val userType = userTypeRepository.findByCode(Constants.DEFAULT_USER_TYPE_CODE)
        val password = signUpUserForm.password ?: generateRandPassword()
        val encodedPassword = passwordEncoder.encode(password)

        val user = User(
            userType = userType!!,
            email = signUpUserForm.email,
            name = signUpUserForm.name,
            phone = signUpUserForm.phone,
            password = encodedPassword)
        userRepository.saveAndFlush(user)

        logger.debug("Successfully registered new user. Add new user to database `users`: $user")

        logger.debug("Create default empty cart for this new user")
        cartService.createNewCartForUser(user)

        return ""
    }

    @Transactional
    fun edit(authorizedUser: User, editForm: SignUpUserForm): String {
        if (!editForm.email.isBlank() && editForm.email != authorizedUser.email && userRepository.existsByEmail(editForm.email))
            return "Failed to edit user. There is already a user with such email: ${editForm.email}. Try another one."

        authorizedUser.email = if (!editForm.email.isBlank()) editForm.email else authorizedUser.email
        authorizedUser.name = if (!editForm.name.isBlank()) editForm.name else authorizedUser.name
        authorizedUser.phone = if (!editForm.phone.isBlank()) editForm.phone else authorizedUser.phone
        authorizedUser.password = if (!editForm.password.isBlank()) passwordEncoder.encode(editForm.password) else authorizedUser.password

        userRepository.save(authorizedUser)
        logger.debug("Successfully edited user. User info: $authorizedUser")

        return ""
    }

    @Throws(AuthenticationException::class)
    @Transactional
    fun findOrRegister(user: User?): User {
        if (user == null)
            throw BadCredentialsException("Empty user params!")

        if (user.email.isBlank())
            throw BadCredentialsException("Email not set!")

        val existedUser = userRepository.findByEmail(user.email)

        var dbUser = user
        if (existedUser != null) {
            dbUser = existedUser
            dbUser.email = if (user.email.isNotBlank() && dbUser.email.isBlank()) user.email else dbUser.email
            dbUser.name = if (user.name.isNotBlank()) user.name else dbUser.name
            dbUser.phone = if (user.phone.isNotBlank()) user.phone else dbUser.phone
        }

        if (dbUser.password == "") {
            val pass = generateRandPassword()
            dbUser.password = passwordEncoder.encode(pass)
            dbUser.externalCode = pass
        }

        userRepository.save(dbUser)
        logger.debug("Successfully edited user. User info: $dbUser")

        return dbUser
    }

    fun getMeForm(user: User): MeUserForm {
        return MeUserForm(
            code = user.code,
            email = user.email,
            name = user.name,
            phone = user.phone,
            password = user.password
        )
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
    }
}