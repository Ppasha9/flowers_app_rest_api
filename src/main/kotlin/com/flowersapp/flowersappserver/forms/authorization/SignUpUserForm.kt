package com.flowersapp.flowersappserver.forms.authorization

import com.flowersapp.flowersappserver.constants.Constants
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class SignUpUserForm(
    @field:Size(max = Constants.EMAIL_MAX_LENGTH)
    @field:Email
    @field:NotBlank
    var email: String = "",

    @field:Size(min = Constants.PASSWORD_MIN_LENGTH, max = Constants.STRING_LENGTH_MIDDLE)
    @field:NotBlank
    var password: String = "",

    @field:Size(min = Constants.NAME_MIN_LENGTH, max = Constants.NAME_MAX_LENGTH)
    @field:NotBlank
    var name: String = "",

    @field:NotBlank
    var phone: String = ""
)