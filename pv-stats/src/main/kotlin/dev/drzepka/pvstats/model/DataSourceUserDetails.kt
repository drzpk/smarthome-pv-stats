package dev.drzepka.pvstats.model

import dev.drzepka.pvstats.Role
import dev.drzepka.pvstats.entity.DataSource
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class DataSourceUserDetails(val dataSource: DataSource) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = mutableListOf(SimpleGrantedAuthority(Role.VIEWER))

    override fun isEnabled(): Boolean = true

    override fun getUsername(): String = dataSource.user

    override fun isCredentialsNonExpired(): Boolean = true

    override fun getPassword(): String = dataSource.password

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true
}