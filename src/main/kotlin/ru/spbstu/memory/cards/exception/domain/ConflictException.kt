package ru.spbstu.memory.cards.exception.domain

import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription

class ConflictException(
    val code: ApiErrorCode,
    message: String = ApiErrorDescription.CONFLICT.description,
) : DomainException(message)
