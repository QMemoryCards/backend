package ru.spbstu.memory.cards.exception.api

enum class ApiErrorDescription(val description: String) {
    VALIDATION_ERROR("Проверьте корректность введённых данных"),
    UNAUTHORIZED("Не авторизован"),
    FORBIDDEN("Доступ запрещён"),
    NOT_FOUND("Ресурс не найден"),
    CONFLICT("Ресурс уже существует"),
    LIMIT_EXCEEDED("Достигнут максимальный лимит"),
    INTERNAL_ERROR("Внутренняя ошибка сервера"),
}
