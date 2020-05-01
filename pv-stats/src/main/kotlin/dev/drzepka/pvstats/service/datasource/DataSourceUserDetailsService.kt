package dev.drzepka.pvstats.service.datasource

import dev.drzepka.pvstats.model.DataSourceUserDetails
import dev.drzepka.pvstats.repository.DataSourceRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

class DataSourceUserDetailsService(private val dataSourceRepository: DataSourceRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        val dataSource = dataSourceRepository.findByUser(username!!) ?: throw UsernameNotFoundException(username)
        return DataSourceUserDetails(dataSource)
    }
}