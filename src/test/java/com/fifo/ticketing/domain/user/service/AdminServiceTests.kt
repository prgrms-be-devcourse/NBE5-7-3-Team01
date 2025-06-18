package com.fifo.ticketing.domain.user.service

import com.fifo.ticketing.domain.user.entity.Role
import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.domain.user.repository.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.util.*

@ExtendWith(MockKExtension::class)
internal class AdminServiceTests {
    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var adminService: AdminService

    private lateinit var pageable: Pageable

    private lateinit var userList: MutableList<User>

    @BeforeEach
    fun setUp() {
        pageable = PageRequest.of(0, 5)

        userList = ArrayList()
        for (i in 0..2) {
            userList.add(
                User(
                    id = i.toLong(),
                    username = "test$i",
                    email = "test$i@test.com"
                )
            )
        }
    }

    @Test
    fun view_all_users() {
        val userPage: Page<User> = PageImpl(userList, pageable, userList.size.toLong())

        every { userRepository.findAll(pageable) } returns userPage

        val allUsers = adminService.getAllUsers(pageable)

        assertThat(allUsers.totalElements).isEqualTo(3)
        assertThat(allUsers.content.first().username).isEqualTo("test0")
    }

    @Test
    fun search_by_username() {
        val userPage: Page<User> = PageImpl(userList, pageable, userList.size.toLong())

        every { userRepository.findByUsernameContaining("test", pageable) } returns userPage

        val nameData = adminService.getUsersByName(pageable, "test")

        assertThat(nameData.totalElements).isEqualTo(3)
        assertThat(nameData.content.first().username).isEqualTo("test0")
    }

    @Test
    fun search_by_role() {
        val userPage: Page<User> = PageImpl(userList, pageable, userList.size.toLong())

        every { userRepository.findByRole(Role.USER, pageable) } returns userPage

        val roleData = adminService.getUsersByRole(
            pageable, Role.USER
        )

        assertThat(roleData.totalElements).isEqualTo(3)
        assertThat(roleData.content.first().username).isEqualTo("test0")
    }

    @Test
    fun search_by_username_and_role() {
        val userPage: Page<User> = PageImpl(userList, pageable, userList.size.toLong())

        every { userRepository.findByUsernameContainingAndRole("test", Role.USER, pageable) } returns userPage

        val nameAndRoleData = adminService.getUsersByRoleAndName(
            pageable, Role.USER,
            "test"
        )

        assertThat(nameAndRoleData.content).hasSize(3)
        assertThat(nameAndRoleData.content.first().username).startsWith("test")
    }

    @Test
    fun check_user_update() {
        val user = User(
            id = 1L,
            email = "test@test.com",
            username = "test"
        )
        every { userRepository.findById(1L) } returns Optional.of(user)

        adminService.updateUserStatus(user.id!!)

        assertThat(user.isBlocked).isTrue()
    }
}