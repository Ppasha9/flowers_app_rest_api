package com.flowersapp.flowersappserver.authorization

import com.flowersapp.flowersappserver.datatables.users.User

class JwtResponse(val accessToken: String, user: User) {
    val token = accessToken
    val tokenType = "Bearer"
    val email = user.email
    val name = user.name
    val surname = user.surname
    val phone = user.phone
}