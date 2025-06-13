package com.fifo.ticketing.domain.user.service

import com.fifo.ticketing.domain.user.dto.oauth.UserOAuthDetails
import com.fifo.ticketing.domain.user.entity.OAuthProvider
import org.springframework.security.oauth2.core.user.OAuth2User

object UserDetailsFactory {
    @JvmStatic
    fun memberFormDetails(provider: String, oAuth2User: OAuth2User?): UserOAuthDetails {
        return OAuthProvider.from(provider).extract(oAuth2User)
    }
}
