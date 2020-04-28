package dev.drzepka.pvstats.autoconfiguration

import dev.drzepka.pvstats.config.TerminalConfig
import dev.drzepka.pvstats.repository.DataSourceRepository
import dev.drzepka.pvstats.service.datasource.DataSourceUserDetailsService
import dev.drzepka.pvstats.util.CompositeUserDetailsService
import dev.drzepka.pvstats.util.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager

@Configuration
@EnableWebSecurity
class SecurityAutoConfiguration(
        private val terminalConfig: TerminalConfig,
        private val dataSourceRepository: DataSourceRepository
) : WebSecurityConfigurerAdapter() {

    private val log by Logger()

    override fun configure(http: HttpSecurity?) {
        http!!.authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .defaultSuccessUrl("/terminal", true)
                .permitAll()
                .and()
                .httpBasic()
                .and()
                .csrf().disable()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    override fun userDetailsService(): UserDetailsService {
        val manager = InMemoryUserDetailsManager()
        addUsers(manager)
        val userDetails = DataSourceUserDetailsService(dataSourceRepository)
        return CompositeUserDetailsService(listOf(userDetails, manager))
    }

    private fun addUsers(manager: InMemoryUserDetailsManager) {
        terminalConfig.users.forEach { terminalUser ->
            val user = User.withUsername(terminalUser.login)
                    .passwordEncoder(passwordEncoder()::encode).password(terminalUser.passwordSha256)
                    .authorities(terminalUser.roles.map { SimpleGrantedAuthority(it) })
                    .build()
            manager.createUser(user)
        }
        log.info("Added ${terminalConfig.users.size} user(s)")
    }
}