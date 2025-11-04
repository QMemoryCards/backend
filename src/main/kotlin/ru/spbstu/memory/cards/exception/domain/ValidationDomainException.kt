package ru.spbstu.memory.cards.exception.domain

import ru.spbstu.memory.cards.exception.api.ApiErrorDescription

class ValidationDomainException(
    message: String = ApiErrorDescription.VALIDATION_ERROR.description,
    val details: Map<String, Any?>? = null,
) : DomainException(message)
