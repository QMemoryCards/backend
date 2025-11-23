package ru.spbstu.memory.cards.exception.domain

import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription

class LimitExceededException(
    val code: ApiErrorCode,
    message: String = ApiErrorDescription.LIMIT_EXCEEDED.description,
    val details: Map<String, Any?>? = null,
) : DomainException(message)
