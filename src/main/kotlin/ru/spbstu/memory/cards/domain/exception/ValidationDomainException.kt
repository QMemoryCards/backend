package ru.spbstu.memory.cards.domain.exception

import ru.spbstu.memory.cards.exception.model.ApiErrorDescription

class ValidationDomainException(
    message: String = ApiErrorDescription.VALIDATION_ERROR.description,
    val details: Map<String, Any?>? = null,
) : DomainException(message)
