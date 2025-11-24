package ru.spbstu.memory.cards.persistence

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import ru.spbstu.memory.cards.persistence.mapper.toDeckShareInsert
import ru.spbstu.memory.cards.persistence.table.DeckSharesTable
import java.util.UUID

@Repository
class DeckSharesRepository {
    fun saveToken(
        token: UUID,
        deckId: UUID,
    ) {
        transaction {
            DeckSharesTable.insert {
                it.toDeckShareInsert(deckId = deckId, token = token)
            }
        }
    }

    fun findByToken(token: UUID): UUID? {
        return transaction {
            DeckSharesTable.selectAll()
                .where { DeckSharesTable.token eq token }
                .map { it[DeckSharesTable.deckId] }
                .firstOrNull()
        }
    }

    fun deleteToken(token: UUID) {
        transaction {
            DeckSharesTable.deleteWhere { DeckSharesTable.token eq token }
        }
    }
}
