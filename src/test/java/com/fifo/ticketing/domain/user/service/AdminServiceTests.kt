package com.fifo.ticketing.domain.user.service

import com.fifo.ticketing.domain.user.dto.UserDto.username
import com.fifo.ticketing.domain.user.entity.Role
import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.domain.user.entity.User.id
import com.fifo.ticketing.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class AdminServiceTests {
    @Mock
    private val userRepository: UserRepository? = null

    @InjectMocks
    private val adminService: AdminService? = null

    private var pageable: Pageable? = null

    private var userList: MutableList<User>? = null

    @BeforeEach
    fun setUp() {
        pageable = PageRequest.of(0, 5)

        userList = ArrayList()
        for (i in 0..2) {
            userList.add(
                User.builder()
                    .id(i.toLong())
                    .username("test$i")
                    .email("test$i@test.com")
                    .build()
            )
        }
    }

    @Test
    fun view_all_users() {
        val userPage: Page<User> = PageImpl(userList, pageable, userList!!.size.toLong())

        Mockito.`when`(
            userRepository!!.findAll(pageable)
        ).thenReturn(userPage)

        val allUsers = adminService!!.getAllUsers(
            pageable!!
        )

        Assertions.assertThat(allUsers.totalElements).isEqualTo(3)
        Assertions.assertThat(allUsers.content.getFirst().username).isEqualTo("test0")
    }

    @Test
    fun search_by_username() {
        val userPage: Page<User> = PageImpl(userList, pageable, userList!!.size.toLong())
        Mockito.`when`(
            userRepository!!.findByUsernameContaining("test", pageable!!)
        ).thenReturn(userPage)

        val nameData = adminService!!.getUsersByName(
            pageable!!, "test"
        )

        Assertions.assertThat(nameData.totalElements).isEqualTo(3)
        Assertions.assertThat(nameData.content.getFirst().username).isEqualTo("test0")
    }

    @Test
    fun search_by_role() {
        val userPage: Page<User> = PageImpl(userList, pageable, userList!!.size.toLong())
        Mockito.`when`(
            userRepository!!.findByRole(Role.USER, pageable!!)
        ).thenReturn(userPage)

        val roleData = adminService!!.getUsersByRole(
            pageable!!, Role.USER
        )

        Assertions.assertThat(roleData.totalElements).isEqualTo(3)
        Assertions.assertThat(roleData.content.getFirst().username).isEqualTo("test0")
    }

    @Test
    fun search_by_username_and_role() {
        val userPage: Page<User> = PageImpl(userList, pageable, userList!!.size.toLong())
        Mockito.`when`(
            userRepository!!.findByUsernameContainingAndRole(
                "test", Role.USER,
                pageable!!
            )
        ).thenReturn(
            userPage
        )

        val nameAndRoleData = adminService!!.getUsersByRoleAndName(
            pageable!!, Role.USER,
            "test"
        )

        Assertions.assertThat(nameAndRoleData.content).hasSize(3)
        Assertions.assertThat(nameAndRoleData.content.getFirst().username).startsWith("test")
    }

    @Test
    fun check_user_update() {
        val user: User = User.builder()
            .id(1L)
            .email("test@test.com")
            .username("test")
            .build()

        Mockito.`when`(
            userRepository!!.findById(1L)
        ).thenReturn(Optional.of(user))

        adminService!!.updateUserStatus(user.id!!)

        Assertions.assertThat(user.isBlocked).isTrue()
    }
}