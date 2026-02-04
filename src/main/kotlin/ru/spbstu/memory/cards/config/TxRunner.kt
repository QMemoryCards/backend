package ru.spbstu.memory.cards.config

interface TxRunner {
    fun <T> required(block: () -> T): T
}
