package ru.spbstu.memory.cards.persistence.model

data class PaginatedResult<T>(
    val items: List<T>,
    val total: Long,
)
