package ru.spbstu.memory.cards.config

import org.jetbrains.exposed.sql.Database
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class DatabaseConfig {
    @Bean
    fun exposedDatabase(dataSource: DataSource): Database = Database.connect(dataSource)

    @Bean
    fun txManager(dataSource: DataSource): PlatformTransactionManager = DataSourceTransactionManager(dataSource)
}
