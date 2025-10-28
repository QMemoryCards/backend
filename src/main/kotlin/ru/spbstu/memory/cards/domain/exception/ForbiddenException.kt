package ru.spbstu.memory.cards.domain.exception

import ru.spbstu.memory.cards.exception.model.ApiErrorDescription

class ForbiddenException(
    message: String = ApiErrorDescription.FORBIDDEN.description,
) : DomainException(message)
