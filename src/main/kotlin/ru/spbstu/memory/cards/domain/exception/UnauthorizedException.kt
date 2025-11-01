package ru.spbstu.memory.cards.domain.exception

import ru.spbstu.memory.cards.exception.model.ApiErrorDescription

class UnauthorizedException(
    message: String = ApiErrorDescription.UNAUTHORIZED.description,
) : DomainException(message)
