package ru.spbstu.memory.cards.service.auth.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import ru.spbstu.memory.cards.persistence.model.User

class AppUserDetails(
    private val user: User,
) : UserDetails {
    override fun getUsername(): String = user.login

    override fun getPassword(): String = user.passwordHash

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        mutableListOf(SimpleGrantedAuthority("ROLE_USER"))

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true

    fun getId() = user.id

    fun getEmail() = user.email

    fun getCreatedAt() = user.createdAt
}
