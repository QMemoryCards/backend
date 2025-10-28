package ru.spbstu.memory.cards.domain.exception

import ru.spbstu.memory.cards.exception.model.ApiErrorDescription

class LimitExceededException(
    message: String = ApiErrorDescription.LIMIT_EXCEEDED.description,
    val details: Map<String, Any?>? = null,
) : DomainException(message)
