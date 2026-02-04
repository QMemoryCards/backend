package ru.spbstu.memory.cards.config

import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Component

@Component
class ExposedTxRunner : TxRunner {
    override fun <T> required(block: () -> T): T = transaction { block() }
}
