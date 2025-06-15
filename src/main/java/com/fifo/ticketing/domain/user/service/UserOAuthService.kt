package com.fifo.ticketing.domain.user.service

import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.domain.user.repository.UserRepository
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.OAuth2ExceptionFactory
import jakarta.transaction.Transactional
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
@Transactional
class UserOAuthService(
    private val userRepository: UserRepository
) : DefaultOAuth2UserService() {

    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val provider = userRequest.clientRegistration.registrationId
        val memberOAuthDetails = UserDetailsFactory.memberFormDetails(
            provider,
            oAuth2User
        )
        val existingUser = userRepository.findByEmail(memberOAuthDetails.email)
        val user = existingUser ?: userRepository.save(
            User.fromOAuth(
                memberOAuthDetails.email,
                memberOAuthDetails.name,
                provider
            )
        )
        if (user.provider == null || user.provider != provider) {
            throw OAuth2ExceptionFactory.fromErrorCode(
                ErrorCode.EMAIL_ALREADY_REGISTERED_WITH_DIFFERENT_PROVIDER
            )
        }
        memberOAuthDetails.role = user.role
        return memberOAuthDetails
    }
}
