package com.flowersapp.flowersappserver.services.users

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.datatables.products.Product
import com.flowersapp.flowersappserver.datatables.users.*
import com.flowersapp.flowersappserver.forms.authorization.MeUserForm
import com.flowersapp.flowersappserver.forms.authorization.SignUpUserForm
import com.flowersapp.flowersappserver.forms.authorization.UserAdminPanelForm
import com.flowersapp.flowersappserver.forms.delivery_address.DeliveryAddressForm
import com.flowersapp.flowersappserver.forms.delivery_address.DeliveryAddressGetForm
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
    private lateinit var userToDeliveryAddressRepository: UserToDeliveryAddressRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Transactional
    fun findByEmail(email: String): User? = userRepository.findByEmail(email)

    @Transactional
    fun findByEmailOrPhone(emailOrPhone: String): User? = userRepository.findByEmail(emailOrPhone) ?: userRepository.findByPhone(emailOrPhone)

    @Transactional
    fun save(user: User) = userRepository.save(user)

    @Transactional
    fun getTotalNumCasual(): Int {
        val allUsers = userRepository.findAll()
        val casualUsers = allUsers.filter { user -> user.userType.code == Constants.DEFAULT_USER_TYPE_CODE }
        return casualUsers.count()
    }

    @Transactional
    fun getTotalNumAdmin(): Int {
        val allUsers = userRepository.findAll()
        val adminUsers = allUsers.filter { user -> user.userType.code == Constants.ADMIN_USER_TYPE_CODE }
        return adminUsers.count()
    }

    fun getCurrentAuthorizedUser(): User? = findByEmail(SecurityContextHolder.getContext().authentication.name)

    fun generateRandPassword(passwordLength: Int = Constants.STRING_LENGTH_SHORT) = generateRandStr()

    @Transactional
    fun signUp(signUpUserForm: SignUpUserForm): String {
        if (signUpUserForm.email.isBlank())
            return "Failed to execute sign up. Field `email` is required, but it is empty in this request."

        if (!signUpUserForm.email.isBlank() && userRepository.existsByEmail(signUpUserForm.email))
            return "Failed to execute sign up. There is already a user with email ${signUpUserForm.email}"

        val userType = userTypeRepository.findByCode(Constants.DEFAULT_USER_TYPE_CODE)
        val password = signUpUserForm.password
        val encodedPassword = passwordEncoder.encode(password)

        val user = User(
            userType = userType!!,
            email = signUpUserForm.email,
            name = signUpUserForm.name,
            surname = signUpUserForm.surname,
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
        authorizedUser.surname = if (!editForm.surname.isBlank()) editForm.surname else authorizedUser.surname
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
            dbUser.surname = if (user.surname.isNotBlank()) user.surname else dbUser.surname
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
            surname = user.surname,
            phone = user.phone,
            password = user.password
        )
    }

    @Transactional
    fun addDeliveryAddress(form: DeliveryAddressForm, user: User): String? {
        if (userToDeliveryAddressRepository.existsByUserCodeAndAddressName(user.code, form.name)) {
            return "Address with name ${form.name} already exists for user with code ${user.code}"
        }

        userToDeliveryAddressRepository.saveAndFlush(
            UserToDeliveryAddress(
                userCode = user.code,
                addressName = form.name,
                houseAddress = form.houseAddress,
                flatNum = form.flatNum,
                entranceNum = form.entranceNum,
                floorNum = form.floorNum,
                comment = form.comment ?: ""
            )
        )

        return null
    }

    @Transactional
    fun editDeliveryAddress(form: DeliveryAddressForm, user: User): String? {
        if (!userToDeliveryAddressRepository.existsByUserCodeAndAddressName(user.code, form.name)) {
            return "Address with name ${form.name} doesn't exists for user with code ${user.code}"
        }

        val objToEdit = userToDeliveryAddressRepository.findByUserCodeAndAddressName(user.code, form.name)!!
        objToEdit.addressName = if (form.name == "") objToEdit.addressName else form.name
        objToEdit.houseAddress = if (form.houseAddress == "") objToEdit.houseAddress else form.houseAddress
        objToEdit.flatNum = if (form.flatNum == 0) objToEdit.flatNum else form.flatNum
        objToEdit.entranceNum = if (form.entranceNum == 0) objToEdit.entranceNum else form.entranceNum
        objToEdit.floorNum = if (form.floorNum == 0) objToEdit.floorNum else form.floorNum
        objToEdit.comment = if (form.comment.isNullOrBlank()) objToEdit.comment else form.comment

        userToDeliveryAddressRepository.saveAndFlush(objToEdit)

        return null
    }

    @Transactional
    fun deleteDeliveryAddress(addressName: String, user: User) {
        if (!userToDeliveryAddressRepository.existsByUserCodeAndAddressName(user.code, addressName)) {
            return
        }

        userToDeliveryAddressRepository.delete(userToDeliveryAddressRepository.findByUserCodeAndAddressName(user.code, addressName)!!)
    }

    @Transactional
    fun getAllDeliveryAddressesForms(user: User): DeliveryAddressGetForm {
        val res = DeliveryAddressGetForm(
            addresses = arrayListOf()
        )

        userToDeliveryAddressRepository.findByUserCode(user.code).forEach {
            res.addresses.add(DeliveryAddressForm(
                name = it.addressName,
                houseAddress = it.houseAddress,
                flatNum = it.flatNum,
                floorNum = it.floorNum,
                entranceNum = it.entranceNum,
                comment = it.comment ?: ""
            ))
        }

        return res
    }

    fun getMeAdminPanelForm(user: User): UserAdminPanelForm {
        return UserAdminPanelForm(
            id = user.code,
            name = user.name,
            surname = user.surname,
            email = user.email,
            phone = user.phone
        )
    }

    @Transactional
    fun getByRange(range: ArrayList<Long>?): List<User> {
        val users = userRepository.findAll()
        val casualUsers = users.filter { user -> user.userType.code == Constants.DEFAULT_USER_TYPE_CODE }
        if (range != null) {
            val newCasualUsers = arrayListOf<User>()
            casualUsers.forEachIndexed { index, user ->
                run {
                    if (index >= range[0] && index <= range[1]) {
                        newCasualUsers.add(user)
                    }
                }
            }
            return newCasualUsers
        }
        return casualUsers
    }

    @Transactional
    fun getAdminsByRange(range: ArrayList<Long>?): List<User> {
        val users = userRepository.findAll()
        val adminUsers = users.filter { user -> user.userType.code == Constants.ADMIN_USER_TYPE_CODE }
        if (range != null) {
            val newAdminUsers = arrayListOf<User>()
            adminUsers.forEachIndexed { index, user ->
                run {
                    if (index >= range[0] && index <= range[1]) {
                        newAdminUsers.add(user)
                    }
                }
            }
            return newAdminUsers
        }
        return adminUsers
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
    }
}