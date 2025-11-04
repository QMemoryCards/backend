package ru.spbstu.memory.cards.exception.domain

import ru.spbstu.memory.cards.exception.api.ApiErrorDescription

class ConflictException(
    message: String = ApiErrorDescription.CONFLICT.description,
) : DomainException(message)
