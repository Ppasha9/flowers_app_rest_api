package com.flowersapp.flowersappserver.utils

import com.flowersapp.flowersappserver.constants.Constants
import java.util.*
import kotlin.streams.asSequence

fun generateRandStr(
    length: Int = Constants.STRING_LENGTH_SHORT,
    lang: String = Constants.ENGLISH_LOCALE_NAME
) : String {
    val source = when (lang) {
        "eng" -> Constants.ENGLISH_SYMBOLS
        else -> Constants.RUSSIAN_SYMBOLS
    }

    return Random().ints(length.toLong(), 0, source.length)
        .asSequence()
        .map(source::get)
        .joinToString("")
}
