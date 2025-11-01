package ru.spbstu.memory.cards.exception.domain

import ru.spbstu.memory.cards.exception.api.ApiErrorDescription

class UnauthorizedException(
    message: String = ApiErrorDescription.UNAUTHORIZED.description,
) : DomainException(message)
