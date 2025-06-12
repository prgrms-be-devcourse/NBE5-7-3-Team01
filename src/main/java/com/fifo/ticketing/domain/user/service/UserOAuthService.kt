package com.fifo.ticketing.domain.user.service

import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.domain.user.repository.UserRepository
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.OAuth2ExceptionFactory
import jakarta.transaction.Transactional
import lombok.RequiredArgsConstructor
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.util.*

@Service
@Transactional
@RequiredArgsConstructor
class UserOAuthService : DefaultOAuth2UserService() {
    private val userRepository: UserRepository? = null

    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val provider = userRequest.clientRegistration.registrationId
        val memberOAuthDetails = UserDetailsFactory.memberFormDetails(
            provider,
            oAuth2User
        )
        val userOptional: Optional<User>? = userRepository!!.findByEmail(memberOAuthDetails.email)
        val user = userOptional!!.orElseGet {
            val saved =
                User.builder()
                    .email(memberOAuthDetails.email)
                    .username(memberOAuthDetails.name)
                    .provider(provider)
                    .build()
            userRepository.save(saved)
        }
        if (user.provider == null || user.provider != provider) {
            throw OAuth2ExceptionFactory.fromErrorCode(
                ErrorCode.EMAIL_ALREADY_REGISTERED_WITH_DIFFERENT_PROVIDER
            )
        }
        memberOAuthDetails.role = user.role
        return memberOAuthDetails
    }
}
