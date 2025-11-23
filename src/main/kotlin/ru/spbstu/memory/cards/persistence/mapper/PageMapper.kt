package ru.spbstu.memory.cards.persistence.mapper

import ru.spbstu.memory.cards.dto.response.PageResponse
import ru.spbstu.memory.cards.persistence.model.PaginatedResult
import kotlin.math.ceil

object PageMapper {
    fun <T> toPageResponse(
        paginatedResult: PaginatedResult<T>,
        page: Int,
        size: Int,
    ): PageResponse<T> {
        val totalPages =
            if (size > 0) {
                ceil(paginatedResult.total.toDouble() / size).toInt()
            } else {
                0
            }

        return PageResponse(
            content = paginatedResult.items,
            totalElements = paginatedResult.total,
            totalPages = totalPages,
            page = page,
            size = size,
        )
    }
}
