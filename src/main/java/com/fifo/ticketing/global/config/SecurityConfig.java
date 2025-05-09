package com.fifo.ticketing.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .logout(logout -> {
          logout.logoutUrl("/signout")
              .logoutSuccessUrl("/signin")
              .clearAuthentication(true)
              .invalidateHttpSession(true)
              .permitAll();
        })
        .oauth2Login(oauth -> {
          oauth.loginPage("/oauth/login")
              .defaultSuccessUrl("/", true); //추후 수정 필요 로그인 성공시 #todo
        })
        .authorizeHttpRequests(auth -> {
          auth.requestMatchers("/index", "/")
              .permitAll()
              .requestMatchers("signin", "/login", "/sign-up", "/signup", "/oauth/login")
              .anonymous()
              .requestMatchers("/user/**")
              .hasAnyAuthority("USER", "ADMIN")
              .requestMatchers("/admin/**")
              .hasAnyAuthority("ADMIN")
              .anyRequest()
              .authenticated();
        })
        .build();
  }
}
