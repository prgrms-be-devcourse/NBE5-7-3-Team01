package com.fifo.ticketing.domain.user.entity

import com.fifo.ticketing.global.entity.BaseDateEntity
import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.NoArgsConstructor

@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var email: String,

    var password: String? = null,

    @Column(nullable = false)
    var username: String,

    var provider: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER,

    @Column(nullable = false)
    var isBlocked: Boolean = false
) : BaseDateEntity() {


    fun updateBlockedState() {
        isBlocked = !isBlocked
    }

    companion object {
        fun fromForm(email: String, password: String, username: String): User =
            User(email = email, password = password, username = username)


        fun fromOAuth(email: String, username: String, provider: String): User =
            User(email = email, username =  username, provider =  provider)

    }
}
