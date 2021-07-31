package com.flowersapp.flowersappserver.configurers.web_security

import com.flowersapp.flowersappserver.authorization.JwtAuthEntryPoint
import com.flowersapp.flowersappserver.authorization.JwtAuthTokenFilter
import com.flowersapp.flowersappserver.authorization.JwtUserDetailsServiceImpl
import com.flowersapp.flowersappserver.authorization.OAuth2AuthenticationSuccessHandler
import com.flowersapp.flowersappserver.services.users.OAuth2UserServiceImpl
import com.flowersapp.flowersappserver.services.users.OidcUserServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfigurer : WebSecurityConfigurerAdapter() {
    @Autowired
    private lateinit var userDetailsService: JwtUserDetailsServiceImpl
    @Autowired
    private lateinit var unAuthorizedHandler: JwtAuthEntryPoint

    @Autowired
    private lateinit var oauth2SuccessHandler: OAuth2AuthenticationSuccessHandler
    @Autowired
    private lateinit var oauth2UserService: OAuth2UserServiceImpl
    @Autowired
    private lateinit var oidcUserService: OidcUserServiceImpl

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationJwtTokenFilter(): JwtAuthTokenFilter {
        return JwtAuthTokenFilter()
    }

    @Bean
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder())
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .cors().and().csrf()
                .disable()
            .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                // you can register new user only if you are not logged in
                .antMatchers("/api/auth/signup").not().fullyAuthenticated()
                // you can sign in as any user only if you are not logged in
                .antMatchers("/api/auth/signin").not().fullyAuthenticated()
                .antMatchers("/", "/error", "/webjars/**", "/user", "/login/oauth2/code/google", "/login").permitAll()
                .antMatchers("/api/product/**").permitAll()
                .antMatchers("/api/category/**").permitAll()
                .antMatchers("/api/tag/**").permitAll()
                .antMatchers("/api/cart/**").permitAll()
                .antMatchers("/api/order/**").permitAll()
            // all other urls require authentication
            .anyRequest().authenticated()
            .and()
            .exceptionHandling { e: ExceptionHandlingConfigurer<HttpSecurity?> ->
                e.authenticationEntryPoint(unAuthorizedHandler).and()
                    ?.sessionManagement()?.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.oauth2Login()
                .successHandler(oauth2SuccessHandler)
            .userInfoEndpoint()
                .userService(oauth2UserService)
                .oidcUserService(oidcUserService)

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter::class.java)
    }
}