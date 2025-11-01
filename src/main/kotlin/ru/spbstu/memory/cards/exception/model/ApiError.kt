package ru.spbstu.memory.cards.exception.model

data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any?>? = null,
)
