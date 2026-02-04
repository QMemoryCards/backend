package ru.spbstu.memory.cards.config

import org.springframework.beans.factory.annotation.Autowired
import ru.spbstu.memory.cards.persistence.CardRepository
import ru.spbstu.memory.cards.persistence.DeckRepository
import ru.spbstu.memory.cards.persistence.DeckSharesRepository
import ru.spbstu.memory.cards.persistence.UserRepository

abstract class AbstractIntegrationTest {
    @Autowired
    lateinit var cardRepository: CardRepository

    @Autowired
    lateinit var deckRepository: DeckRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var shareRepository: DeckSharesRepository
}
