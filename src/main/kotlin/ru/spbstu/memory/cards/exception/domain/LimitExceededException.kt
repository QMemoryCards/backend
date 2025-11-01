package ru.spbstu.memory.cards.exception.domain

import ru.spbstu.memory.cards.exception.api.ApiErrorDescription

class LimitExceededException(
    message: String = ApiErrorDescription.LIMIT_EXCEEDED.description,
    val details: Map<String, Any?>? = null,
) : DomainException(message)
