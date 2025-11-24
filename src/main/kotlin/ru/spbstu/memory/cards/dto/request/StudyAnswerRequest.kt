package ru.spbstu.memory.cards.dto.request

import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

enum class StudyStatusEnum(
    @JsonValue val typeName: String,
) {
    REMEMBERED("remembered"),
    FORGOTTEN("forgotten"),
}

data class StudyAnswerRequest(
    val cardId: UUID,
    val status: StudyStatusEnum,
)
