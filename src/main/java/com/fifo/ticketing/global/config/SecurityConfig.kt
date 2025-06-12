package com.fifo.ticketing.global.config

import com.fifo.ticketing.domain.user.service.handler.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val formLoginSuccessHandler: FormLoginSuccessHandler,
    private val oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
    private val formLoginFailureHandler: FormLoginFailureHandler,
    private val oAuth2LoginFailureHandler: OAuth2LoginFailureHandler,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler
) {

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .formLogin {
                it.loginPage("/users/signin")
                    .usernameParameter("email")
                    .passwordParameter("loginPwd")
                    .successHandler(formLoginSuccessHandler)
                    .failureHandler(formLoginFailureHandler)
                    .permitAll()
            }
            .logout {
                it.logoutUrl("/signout")
                    .logoutSuccessUrl("/users/signin")
                    .clearAuthentication(true)
                    .invalidateHttpSession(true)
                    .permitAll()
            }
            .oauth2Login {
                it.loginPage("/oauth/login")
                    .successHandler(oAuth2LoginSuccessHandler)
                    .failureHandler(oAuth2LoginFailureHandler)
            }
            .exceptionHandling {
                it.accessDeniedHandler(customAccessDeniedHandler)
            }
            .authorizeHttpRequests {
                it.requestMatchers("/index", "/", "/api/**").permitAll()
                it.requestMatchers(
                    "/users/signin/**",
                    "/users/login/**",
                    "/users/signup/**",
                    "/oauth/login/**"
                ).permitAll()
                it.requestMatchers("/user/**").hasAnyAuthority("USER", "ADMIN")
                it.requestMatchers("/admin/**").hasAnyAuthority("USER")
                it.anyRequest().permitAll()
            }
            .build()
    }
}

