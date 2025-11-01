package ru.spbstu.memory.cards.domain.exception

import ru.spbstu.memory.cards.exception.model.ApiErrorDescription

class ConflictException(
    message: String = ApiErrorDescription.CONFLICT.description,
) : DomainException(message)
