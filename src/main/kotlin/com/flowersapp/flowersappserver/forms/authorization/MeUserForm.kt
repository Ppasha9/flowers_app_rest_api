package com.flowersapp.flowersappserver.forms.authorization

import com.flowersapp.flowersappserver.constants.Constants
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class MeUserForm(
    @field:Size(max = Constants.STRING_LENGTH_LONG)
    @field:NotBlank
    var code: String = "",

    @field:Size(max = Constants.EMAIL_MAX_LENGTH)
    @field:Email
    @field:NotBlank
    var email: String = "",

    @field:Size(min = Constants.NAME_MIN_LENGTH, max = Constants.NAME_MAX_LENGTH)
    @field:NotBlank
    var name: String = "",

    @field:Size(min = Constants.NAME_MIN_LENGTH, max = Constants.NAME_MAX_LENGTH)
    @field:NotBlank
    var surname: String = "",

    @field:NotBlank
    var phone: String = "",

    @field:Size(min = Constants.PASSWORD_MIN_LENGTH, max = Constants.STRING_LENGTH_MIDDLE)
    @field:NotBlank
    var password: String = ""
)