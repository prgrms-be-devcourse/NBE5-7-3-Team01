package com.fifo.ticketing.domain.book.controller.view

import com.fifo.ticketing.domain.book.dto.BookCreateRequest
import com.fifo.ticketing.domain.book.service.BookKtService
import com.fifo.ticketing.domain.book.service.BookMailService
import com.fifo.ticketing.domain.book.service.BookService
import com.fifo.ticketing.global.util.UserValidator.validateSessionUser
import jakarta.servlet.http.HttpSession
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequiredArgsConstructor
@RequestMapping("/performances/{performanceId}/book")
class BookController(
    private val bookService: BookService,
    private val bookKtService: BookKtService,
    private val bookMailService: BookMailService,
) {

    @PostMapping
    suspend fun createBook(
        @PathVariable performanceId: Long,
        session: HttpSession,
        @RequestParam seatIds: List<Long>
    ): String {
        val loginUser = validateSessionUser(session)
        val request = BookCreateRequest(seatIds)
        val bookId = bookKtService.createBook(performanceId, loginUser.id, request)
        return "redirect:/performances/$performanceId/book/complete/$bookId"
    }

    @PostMapping("/complete/{bookId}/paid")
    fun completePayment(
        @PathVariable performanceId: Long, @PathVariable bookId: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        bookService.completePayment(bookId)

        val bookMailInfo = bookService.getBookMailInfo(bookId)
        bookMailService.sendBookCompleteMail(bookMailInfo)

        redirectAttributes["paid"] = true
        return "redirect:/performances/$performanceId/book/complete/$bookId"
    }

    @GetMapping("/complete/{bookId}")
    fun viewBookingComplete(
        @PathVariable bookId: Long,
        @RequestParam(value = "paid", required = false, defaultValue = "false") paid: Boolean,
        model: Model
    ): String {
        val bookCompleteInfo = bookService.getBookCompleteInfo(bookId)
        bookCompleteInfo.paymentCompleted = paid

        model["book"] =  bookCompleteInfo
        model["bookId"] =  bookId

        return "book/complete"
    }
}
