package ru.spbstu.memory.cards.exception.model

enum class ApiErrorCode(val code: String) {
    VALIDATION_ERROR("validation_error"),
    UNAUTHORIZED("unauthorized"),
    FORBIDDEN("forbidden"),
    NOT_FOUND("not_found"),
    CONFLICT("conflict"),
    LIMIT_EXCEEDED("limit_exceeded"),
    INTERNAL_ERROR("internal_error"),
}
