package com.fifo.ticketing.domain.user.dto.oauth

import com.fifo.ticketing.domain.user.entity.Role
import lombok.Builder
import lombok.Getter
import lombok.Setter
import lombok.experimental.Accessors
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User
import java.util.List

@Getter
@Accessors(chain = true)
class UserOAuthDetails @Builder constructor(
    private val name: String,
    private val email: String,
    private val attributes: Map<String, Any>,
    @field:Setter private val role: Role
) :
    OAuth2User {
    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return List.of(SimpleGrantedAuthority(role.name))
    }
}
