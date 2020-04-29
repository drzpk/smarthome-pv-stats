package dev.drzepka.pvstats.service.datasource

import dev.drzepka.pvstats.Role
import dev.drzepka.pvstats.repository.DataSourceRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

class DataSourceUserDetailsService(private val dataSourceRepository: DataSourceRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        val dataSource = dataSourceRepository.findByUser(username!!) ?: throw UsernameNotFoundException(username)

        return object : UserDetails {
            override fun getAuthorities(): MutableCollection<out GrantedAuthority> = mutableListOf(SimpleGrantedAuthority(Role.VIEWER))

            override fun isEnabled(): Boolean = true

            override fun getUsername(): String = dataSource.user

            override fun isCredentialsNonExpired(): Boolean = true

            override fun getPassword(): String = dataSource.password

            override fun isAccountNonExpired(): Boolean = true

            override fun isAccountNonLocked(): Boolean = true
        }
    }
}