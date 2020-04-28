package dev.drzepka.pvstats.util

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

class CompositeUserDetailsService(private val delegates: List<UserDetailsService>) : UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        if (delegates.isEmpty())
            throw AccessDeniedException("No UserDetailsService delegates were defined")

        for (i in delegates.indices) {
            try {
                return delegates[i].loadUserByUsername(username)
            } catch (e: Exception) {
                if (i == delegates.size - 1) {
                    // No more delegates
                    throw e
                }
            }
        }

        throw AccessDeniedException("Impossible")
    }
}