package ru.spbstu.memory.cards.domain.exception

import ru.spbstu.memory.cards.exception.model.ApiErrorDescription

class NotFoundException(
    message: String = ApiErrorDescription.NOT_FOUND.description,
) : DomainException(message)
