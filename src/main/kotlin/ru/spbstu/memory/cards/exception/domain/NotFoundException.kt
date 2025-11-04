package ru.spbstu.memory.cards.exception.domain

import ru.spbstu.memory.cards.exception.api.ApiErrorDescription

class NotFoundException(
    message: String = ApiErrorDescription.NOT_FOUND.description,
) : DomainException(message)
