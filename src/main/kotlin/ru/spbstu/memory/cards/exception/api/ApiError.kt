package ru.spbstu.memory.cards.exception.api

data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any?>? = null,
)
