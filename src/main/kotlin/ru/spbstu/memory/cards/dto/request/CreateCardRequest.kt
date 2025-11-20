package ru.spbstu.memory.cards.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateCardRequest(
    @field:NotBlank
    @field:Size(max = 200)
    @field:Pattern(
        regexp = "^[A-Za-zА-Яа-я0-9\\s~`!@#$%^&*()_\\-+={\\[}\\]|\\\\:;\"'<,>.?/]*$",
        message = "Вопрос содержит недопустимые символы",
    )
    val question: String,
    @field:NotBlank
    @field:Size(max = 200)
    @field:Pattern(
        regexp = "^[A-Za-zА-Яа-я0-9\\s~`!@#$%^&*()_\\-+={\\[}\\]|\\\\:;\"'<,>.?/]*$",
        message = "Ответ содержит недопустимые символы",
    )
    val answer: String,
)
