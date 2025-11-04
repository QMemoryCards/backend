package ru.spbstu.memory.cards.service.auth

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.persistence.UserRepository
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails

@Service
class AppUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails =
        userRepository.findByLogin(username)?.let {
            AppUserDetails(it)
        } ?: throw UsernameNotFoundException(ApiErrorCode.NOT_FOUND.code)
}
