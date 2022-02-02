package com.flowersapp.flowersappserver.controllers

import com.flowersapp.flowersappserver.authorization.JwtProvider
import com.flowersapp.flowersappserver.authorization.JwtResponse
import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.datatables.users.User
import com.flowersapp.flowersappserver.datatables.users.UserTypeRepository
import com.flowersapp.flowersappserver.forms.authorization.GoogleSignInForm
import com.flowersapp.flowersappserver.forms.authorization.LoginUserForm
import com.flowersapp.flowersappserver.forms.authorization.SignUpUserForm
import com.flowersapp.flowersappserver.services.users.UserService
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
class AuthController {
    @Autowired
    private lateinit var authenticationManager: AuthenticationManager
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
    @Autowired
    private lateinit var jwtProvider: JwtProvider

    @Autowired
    private lateinit var userService: UserService
    @Autowired
    private lateinit var userTypeRepository: UserTypeRepository

    private val jacksonFactory = JacksonFactory()

    @RequestMapping("/signin", method = [RequestMethod.GET, RequestMethod.POST])
    fun singIn(
        @RequestParam(name = "email", required = false, defaultValue = "") emailOrPhone: String,
        @RequestParam(name = "password", required = false, defaultValue = "") password: String,
        @RequestParam(name = "admin", required = false, defaultValue = false.toString()) admin: Boolean,
        @Valid @RequestBody loginRequest: LoginUserForm?
    ): ResponseEntity<Any> {
        var rPass = password
        var rLogin = emailOrPhone
        if (loginRequest != null) {
            rPass = loginRequest.password
            rLogin = loginRequest.email
        }

        logger.debug("Sign In. EmailOrPhone: {}. Password: {}",
            rLogin,
            passwordEncoder.encode(rPass))

        val user = userService.findByEmailOrPhone(rLogin)
        if (user == null) {
            logger.debug("Sign in failed. No such user by email or phone: {}", rLogin)
            return ResponseEntity("Fail. No such user.", HttpStatus.BAD_REQUEST)
        }

        if (admin && user.userType.code != Constants.ADMIN_USER_TYPE_CODE) {
            logger.debug("Sign in failed. The user {} should have admin permissions", rLogin)
            return ResponseEntity("Fail. The user should have admin permissions", HttpStatus.BAD_REQUEST)
        }

        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(user.email, rPass))
        SecurityContextHolder.getContext().authentication = authentication

        val jwt = jwtProvider.generateJwtToken((authentication.principal as UserDetails).username)

        if (user.isOAuth) {
            user.password = userService.generateRandPassword()
            user.externalCode = ""
            userService.save(user)
        }

        return ResponseEntity.ok(JwtResponse(jwt, user))
    }

    @RequestMapping("/signup", method = [RequestMethod.POST])
    fun signUp(@Valid @RequestBody signUpRequest: SignUpUserForm?): ResponseEntity<Any> {
        logger.debug("Signing up new user started!!!")
        if (signUpRequest == null) {
            logger.error("Invalid sign up request body.")
            return ResponseEntity("Fail. Invalid user sign up request body.", HttpStatus.BAD_REQUEST)
        }

        logger.debug("Trying to sign up new user!!!")
        val errMsg = userService.signUp(signUpRequest)
        if (errMsg.isNotEmpty()) {
            logger.error(errMsg)
            return ResponseEntity(errMsg, HttpStatus.BAD_REQUEST)
        }

        logger.debug("Check that user was added to database")
        val email = signUpRequest.email
        val user = userService.findByEmail(email)
            ?: return ResponseEntity("Cannot register user by Email $email", HttpStatus.BAD_REQUEST)

        logger.debug("Register user. Request: $signUpRequest. User code: ${user.code}")

        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(user.email, signUpRequest.password))
        SecurityContextHolder.getContext().authentication = authentication

        val jwt = jwtProvider.generateJwtToken((authentication.principal as UserDetails).username)
        return ResponseEntity.ok(JwtResponse(jwt, user))
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @RequestMapping("/edit", method = [RequestMethod.POST])
    fun edit(@Valid @RequestBody editRequest: SignUpUserForm?): ResponseEntity<Any> {
        if (editRequest == null) {
            logger.error("Invalid edit request body.")
            return ResponseEntity("Fail. Invalid user edit request body.", HttpStatus.BAD_REQUEST)
        }

        val authorizedUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("No permission. Please, log in before editing.", HttpStatus.BAD_REQUEST)

        logger.debug("Edit user. Request: $editRequest. User code: ${authorizedUser.code}")

        val errMsg = userService.edit(authorizedUser, editRequest)
        if (errMsg.isNotEmpty()) {
            logger.error(errMsg)
            return ResponseEntity(errMsg, HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity.ok("User edited successfully.")
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("/me")
    fun me(): ResponseEntity<Any> {
        logger.debug("Getting me information")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        return ResponseEntity.ok(userService.getMeForm(currUser))
    }

    @RequestMapping("/google_sign_in", method = [RequestMethod.POST, RequestMethod.GET])
    fun googleSignIn(
        @RequestParam(name = "idTokenString", required = false) inputIdTokenString: String?,
        @Valid @RequestBody googleSignInRequest: GoogleSignInForm?
    ): ResponseEntity<Any> {
        val tokenVerifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), jacksonFactory)
            // Specify the CLIENT_ID of the app that accesses the google api backend:
            .setAudience(Collections.singletonList("112191486341-ihqa962ajv7b7bdp7au2vvs4vl8aptt3.apps.googleusercontent.com"))
            .build()

        logger.debug("[Google Sign In.] Request param. inputIdTokenString=$inputIdTokenString")
        logger.debug("[Google Sign In.] Request body. googleSignInRequest.idTokenString=${googleSignInRequest?.idTokenString}")

        val idTokenString = inputIdTokenString ?: googleSignInRequest?.idTokenString ?:
            return ResponseEntity("No idTokenString in input request", HttpStatus.BAD_REQUEST)
        logger.debug("Google Sign In. idTokenString=$idTokenString")
        val idToken = tokenVerifier.verify(idTokenString) ?: return ResponseEntity("Invalid idTokenString", HttpStatus.BAD_REQUEST)

        val payload = idToken.payload
        val userExternalCode = payload.subject
        logger.debug("Google Sign In. User external code: $userExternalCode")

        val password = userService.generateRandPassword()
        val encodedPassword = passwordEncoder.encode(password)

        var user = userService.findByExternalCode(userExternalCode) ?: userService.findByEmail(payload.email)
        if (user == null) {
            val name = payload["given_name"] as String
            val familyName = payload["family_name"] as String

            logger.debug("Goolge Sign In. Create new user: $name $familyName")

            user = User(
                code = "",
                userType = userTypeRepository.findByCode(Constants.DEFAULT_USER_TYPE_CODE)!!,
                email = payload.email,
                name = name,
                surname = familyName,
                password = encodedPassword,
                externalCode = userExternalCode,
                isOAuth = true
            )

            userService.save(user)
            user = userService.findByExternalCode(userExternalCode)!!
        } else {
            user.password = encodedPassword
            userService.save(user)
        }

        logger.debug("Google Sign In. New user login: ${user.name}")

        return try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(user.email, password)
            )

            SecurityContextHolder.getContext().authentication = authentication
            val jwt = jwtProvider.generateJwtToken((authentication.principal as UserDetails).username)
            ResponseEntity(JwtResponse(jwt, user), HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity(e.toString(), HttpStatus.BAD_REQUEST)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(AuthController::class.java)
    }
}