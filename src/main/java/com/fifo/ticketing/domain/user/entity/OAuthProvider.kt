package com.fifo.ticketing.domain.user.entity

import com.fifo.ticketing.domain.user.dto.oauth.UserOAuthDetails
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import org.springframework.security.oauth2.core.user.OAuth2User
import java.util.*

enum class OAuthProvider {
    GOOGLE {
        override fun extract(oAuth2User: OAuth2User): UserOAuthDetails {
            val attributes = oAuth2User.attributes
            return UserOAuthDetails(
                attributes["name"].toString(),
                attributes["email"].toString(),
                attributes,
                Role.USER
            )
        }
    },
    NAVER {
        override fun extract(oAuth2User: OAuth2User): UserOAuthDetails {
            val attributes = oAuth2User.attributes
            val properties = attributes["response"] as Map<String, Any>?
            return UserOAuthDetails(
                properties!!["name"].toString(),
                properties["id"].toString() + "@naver.com",
                properties,
                Role.USER
            )
        }
    },
    KAKAO {
        override fun extract(oAuth2User: OAuth2User): UserOAuthDetails {
            val attributes = oAuth2User.attributes
            val properties = attributes["properties"] as Map<String, Any>?
            return UserOAuthDetails(
                properties!!["nickname"].toString(),
                attributes["id"].toString() + "@kakao.com",
                properties,
                Role.USER
            )
        }
    };

    abstract fun extract(oAuth2User: OAuth2User): UserOAuthDetails

    companion object {
        fun from(provider: String): OAuthProvider {
            try {
                return valueOf(provider.uppercase(Locale.getDefault()))
            } catch (e: IllegalArgumentException) {
                throw ErrorException(ErrorCode.NOT_FOUND_PROVIDER)
            }
        }
    }
}
