package com.fifo.ticketing.domain.user.dto.form

import com.fifo.ticketing.domain.user.entity.Role
import com.fifo.ticketing.domain.user.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserFormDetails(user: User) : UserDetails {
    //이메일과 비밀번호를 통해 인증을 진행중으로 username에는 email 값 할당
    private val email: String = user.email
    private val password: String = user.password
    val role: Role = user.role
    val id: Long? = user.id
    val name: String = user.username

    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return listOf(SimpleGrantedAuthority(role.name))
    }

    override fun getPassword(): String = password

    override fun getUsername(): String = email
}
