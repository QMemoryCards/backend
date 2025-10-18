package ru.spbstu.memory.cards

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["ru.spbstu.memory.cards"],
)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
