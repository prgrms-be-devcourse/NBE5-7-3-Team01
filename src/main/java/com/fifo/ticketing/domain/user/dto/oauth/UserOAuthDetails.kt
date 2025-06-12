package com.fifo.ticketing.domain.user.dto.oauth

import com.fifo.ticketing.domain.user.entity.Role
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

class UserOAuthDetails(
    val username: String,
    val email: String,
    private val userAttributes: Map<String, Any>,
    var role: Role
) :
    OAuth2User {

    override fun getName(): String = username

    override fun getAttributes(): Map<String, Any> = userAttributes

    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return listOf(SimpleGrantedAuthority(role.name))
    }
}
