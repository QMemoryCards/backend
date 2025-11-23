package ru.spbstu.memory.cards.exception.domain

import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription

class NotFoundException(
    val code: ApiErrorCode,
    message: String = ApiErrorDescription.NOT_FOUND.description,
) : DomainException(message)
