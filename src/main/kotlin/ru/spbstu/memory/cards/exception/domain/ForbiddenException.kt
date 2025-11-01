package ru.spbstu.memory.cards.exception.domain

import ru.spbstu.memory.cards.exception.api.ApiErrorDescription

class ForbiddenException(
    message: String = ApiErrorDescription.FORBIDDEN.description,
) : DomainException(message)
