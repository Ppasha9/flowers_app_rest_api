package com.flowersapp.flowersappserver.authorization

import com.flowersapp.flowersappserver.services.users.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class OAuth2AuthenticationSuccessHandler : AuthenticationSuccessHandler {
    @Autowired
    private lateinit var userService: UserService

    override fun onAuthenticationSuccess(p0: HttpServletRequest?, p1: HttpServletResponse?, p2: Authentication?) {
        val signInUrl = "/api/auth/signin?email=%s&password=%s"
        val user = userService.findByEmail(p2!!.name)
        val cred = URLEncoder.encode(user!!.externalCode, "UTF-8")
        val name = URLEncoder.encode(user.email, "UTF-8")

        p1!!.sendRedirect(signInUrl.format(name, cred))
    }
}