package com.flowersapp.flowersappserver.constants

import java.time.OffsetDateTime
import java.time.ZoneOffset

object Constants {
    const val MIN_LENGTH = 3
    const val ALL_EXCEPT_AT_SYMBOL = "([^@])*"
    const val STRING_LENGTH_SHORT = 32
    const val STRING_LENGTH_MIDDLE = 64
    const val STRING_LENGTH_LONG = 256

    const val LOGIN_OR_EMAIL_MIN_LENGTH = 3
    const val PASSWORD_MIN_LENGTH = 6
    const val EMAIL_MAX_LENGTH = 60
    const val NAME_MIN_LENGTH = 3
    const val NAME_MAX_LENGTH = 50
    const val FULL_NAME_MAX_LENGTH = 150

    const val LOCALE_LENGTH = 3

    // Languages
    const val ENGLISH_LOCALE_NAME = "eng"
    const val ENGLISH_SYMBOLS = "AfBbCcDfEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz"
    const val RUSSIAN_LOCALE_NAME = "rus"
    const val RUSSIAN_SYMBOLS = "АаБбВвГгДдЕеЁёЖжЗзИиЙйКкЛлМмНнОоПпРрСсТтУуФфХхЦцЧчШшЩщЪъЫыЬьЭэЮюЯя"

    const val CYRILLIC_TO_LATIN = "Cyrillic-Latin"

    // User types codes
    const val DEFAULT_USER_TYPE_CODE = "default"
    const val ADMIN_USER_TYPE_CODE = "admin"

    // Authority
    const val ANY_AUTHORIZED_AUTHORITY = "hasAuthority('${DEFAULT_USER_TYPE_CODE}') or hasAuthority('${ADMIN_USER_TYPE_CODE}')"
    const val ADMIN_AUTHORIZED_AUTHORITY = "hasAuthority('${ADMIN_USER_TYPE_CODE}')"

    // JWT properties
    const val JWT_SECRET_KEY = "__secrit@p@pas@ha_guy_key__"
    const val JWT_EXPIRATION_TIME_MS = 31622400000000

    val MAX_TIME: OffsetDateTime = OffsetDateTime.of(294270, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

    // PostgreSQL
    const val POSTGRES_SCHEME = "public"

    // Categories codes
    const val CATEGORY_NEW_CODE = "New"
    const val CATEGORY_BOUQUET_CODE = "Bouquet"
    const val CATEGORY_BASKET_CODE = "Basket"

    // Cart's status codes
    const val CART_STATUS_DEFAULT = "Default"
    const val CART_STATUS_RECEIVER_FORMATION = "Receiver_Formation"
    const val CART_STATUS_SHIPPING_FORMATION = "Shipping_Formation"
    const val CART_STATUS_PAYMENT_FORMATION = "Payment_Formation"

    // Order's status codes
    const val ORDER_STATUS_FORMING = "Forming"
    const val ORDER_STATUS_SHIPPING = "Shipping"
    const val ORDER_STATUS_SUCCEEDED = "Succeeded"
    const val ORDER_STATUS_CANCELLED = "Cancelled"

    // Tags codes
    const val TAG_SOFT_CODE = "Нежный"
    const val TAG_BIRTHDAY_CODE = "День Рождения"
    const val TAG_TO_LOVE_CODE = "Любимой"
    const val TAG_TO_MOM_CODE = "Маме"
    const val TAG_ROSES_CODE = "Розы"
    const val TAG_SIMPLY_CODE = "Просто так"
    const val TAG_PINK_CODE = "Розовый"
    const val TAG_BLUE_CODE = "Голубой"
    const val TAG_TO_GIRLFRIEND_CODE = "Подруге"
    const val TAG_FREE_SHIPPMENT_CODE = "Бесплатная доставка"
}