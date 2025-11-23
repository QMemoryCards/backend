package ru.spbstu.memory.cards.exception.api

enum class ApiErrorCode(val code: String) {
    VALIDATION_ERROR("validation_error"),

    UNAUTHORIZED("unauthorized"),

    FORBIDDEN("forbidden"),

    USER_NOT_FOUND("user_not_found"),
    CARD_NOT_FOUND("card_not_found"),
    DECK_NOT_FOUND("deck_not_found"),
    TOKEN_NOT_FOUND("token_not_found"),

    EMAIL_CONFLICT("email_conflict"),
    LOGIN_CONFLICT("login_conflict"),
    DECK_CONFLICT("deck_conflict"),

    CARD_LIMIT("card_limit_exceeded"),
    DECK_LIMIT("deck_limit_exceeded"),

    INTERNAL_ERROR("internal_error"),
}
