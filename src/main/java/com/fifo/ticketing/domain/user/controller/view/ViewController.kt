package com.fifo.ticketing.domain.user.controller.view

import com.fifo.ticketing.domain.book.entity.BookStatus
import com.fifo.ticketing.domain.book.service.BookService
import com.fifo.ticketing.domain.user.dto.SessionUser
import com.fifo.ticketing.domain.user.dto.form.SignUpForm
import com.fifo.ticketing.domain.user.service.UserFormService
import com.fifo.ticketing.global.service.MailService
import com.fifo.ticketing.global.util.UserValidator.validateSessionUser
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class ViewController(
    private val userFormService: UserFormService,
    private val bookService: BookService,
    private val mailService: MailService
) {

    @GetMapping("/")
    fun homePage(session: HttpSession, model: Model): String {
        (session.getAttribute("loginUser") as? SessionUser)?.let {
            model.addAttribute("userRole", it.role.name)
            model.addAttribute("username", it.username)
        }
        return "index"
    }

    @GetMapping("/users/signup")
    fun signup(request: HttpServletRequest): String {
        val loginUser = request.session.getAttribute("loginUser") as? SessionUser
        return if (loginUser != null) "redirect:/" else "user/sign_up"
    }

    @PostMapping("/users/signup")
    fun doSignup(signUpForm: @Valid SignUpForm, session: HttpSession, model: Model): String {
        val emailVerified = session.getAttribute("emailVerified") as? String
        if (emailVerified != signUpForm.email) {
            model.addAttribute("emailVerified", signUpForm.email)
            return "user/sign_up"
        }
        userFormService.save(signUpForm)
        session.removeAttribute("emailVerified")

        return "redirect:/users/signin?signupSuccess=true"
    }

    @GetMapping("/users/signin")
    fun signIn(request: HttpServletRequest, model: Model): String {
        val loginUser = request.session.getAttribute("loginUser") as? SessionUser
        if (loginUser != null) return "redirect:/"

        val errormessage = request.session.getAttribute("errormessage") as? String
        errormessage?.let {
            model.addAttribute("errorMessage", it)
            request.session.removeAttribute("errormessage")
        }

        return "user/sign_in"
    }

    @GetMapping("/users/books")
    fun getBookList(
        session: HttpSession,
        @RequestParam(value = "page", defaultValue = "0", required = false) page: Int,
        @RequestParam(value = "size", defaultValue = "3", required = false) size: Int,
        @RequestParam(required = false) performanceTitle: String?,
        @RequestParam(required = false) bookStatus: BookStatus?,
        model: Model
    ): String {
        val loginUser = validateSessionUser(session)
        val pageable = PageRequest.of(page, size)
        val bookedList = bookService.getBookedList(
            loginUser.id,
            performanceTitle,
            bookStatus,
            pageable
        )

        model.addAttribute("bookedList", bookedList)
        return "user/bookList"
    }

    @GetMapping("/users/books/{bookId}")
    fun getBookDetail(
        session: HttpSession,
        @PathVariable bookId: Long,
        model: Model
    ): String {
        val loginUser = validateSessionUser(session)
        val bookDetail = bookService.getBookDetail(loginUser.id, bookId)

        model.addAttribute("bookDetail", bookDetail)
        model.addAttribute("userName", loginUser.username)

        return "book/detail"
    }


    @DeleteMapping("/users/books/{bookId}")
    fun cancelBook(
        session: HttpSession,
        @PathVariable bookId: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        val loginUser = validateSessionUser(session)
        bookService.cancelBook(bookId, loginUser.id)

        val bookMailInfo = bookService.getBookMailInfo(bookId)
        mailService.sendBookInformationNoticeMail(bookMailInfo)

        redirectAttributes.addFlashAttribute("alertMessage", "예매가 성공적으로 취소되었습니다.")
        return "redirect:/users/books"
    }

    @GetMapping("/users")
    fun myPage(session: HttpSession, model: Model): String {
        val loginUser = validateSessionUser(session)

        model.addAttribute("username", loginUser.username)
        return "user/my_page"
    }

    @GetMapping("/admin/users")
    fun adminUsersPage(session: HttpSession, model: Model?): String {
        validateSessionUser(session)
        return "admin/manage_admin"
    }

    @GetMapping("/admin/menu")
    fun adminMenuPage(session: HttpSession): String {
        validateSessionUser(session)
        return "admin/menu_admin"
    }
}
