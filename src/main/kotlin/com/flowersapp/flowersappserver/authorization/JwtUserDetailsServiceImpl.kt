package com.flowersapp.flowersappserver.authorization

import com.flowersapp.flowersappserver.services.users.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class JwtUserDetailsServiceImpl : UserDetailsService {
    @Autowired
    private lateinit var userService: UserService

    @Throws(UsernameNotFoundException::class)
    @Transactional
    override fun loadUserByUsername(username: String?): UserDetails {
        val strUsername = username ?: throw UsernameNotFoundException("Cannot load user by empty username")
        val user = userService.findByEmail(email = strUsername)
            ?: throw UsernameNotFoundException("User not found with username: $username")
        return JwtUserDetailsImpl(user)
    }
}