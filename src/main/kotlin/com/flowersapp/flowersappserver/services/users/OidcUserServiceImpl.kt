package com.flowersapp.flowersappserver.services.users

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.datatables.users.User
import com.flowersapp.flowersappserver.datatables.users.UserTypeRepository
import com.flowersapp.flowersappserver.datatables.users.toHashMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service

@Service
class OidcUserServiceImpl : OidcUserService() {
    @Autowired
    private lateinit var userService: UserService
    @Autowired
    private lateinit var userTypeRepository: UserTypeRepository

    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OidcUserRequest?): OidcUser {
        val oidcUser = super.loadUser(userRequest)
        val attributes: Map<String, Any> = oidcUser.attributes

        var user = User(
            code = "",
            userType = userTypeRepository.findByCode(Constants.DEFAULT_USER_TYPE_CODE)!!,
            email = attributes["email"] as String,
            name = attributes["given_name"] as String,
            isOAuth = true
        )

        user = userService.findOrRegister(user)

        val authoritySet: Set<GrantedAuthority> = HashSet(setOf(SimpleGrantedAuthority(user.userType.code)))
        val userInfo = OidcUserInfo(user.toHashMap())

        return DefaultOidcUser(authoritySet, oidcUser.idToken, userInfo, "id")
    }
}