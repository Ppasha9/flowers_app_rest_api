package com.flowersapp.flowersappserver.forms.authorization

import com.flowersapp.flowersappserver.constants.Constants
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class LoginUserForm(
    @field:NotBlank
    @field:Size(min = Constants.LOGIN_OR_EMAIL_MIN_LENGTH, max = Constants.STRING_LENGTH_MIDDLE)
    var email: String = "",

    @field:NotBlank
    @field:Size(min = Constants.PASSWORD_MIN_LENGTH, max = Constants.STRING_LENGTH_MIDDLE)
    var password: String = "",

    var admin: Boolean = false
)