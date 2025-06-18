package com.fifo.ticketing.domain.performance.controller.view

import com.fifo.ticketing.domain.book.service.BookService
import com.fifo.ticketing.domain.performance.dto.AdminPerformanceResponseDto
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.service.AdminPerformanceService
import com.fifo.ticketing.domain.seat.service.SeatService
import com.fifo.ticketing.global.util.DateTimeValidator.periodValidator
import com.fifo.ticketing.global.util.UserValidator.validateSessionUser
import jakarta.servlet.http.HttpSession
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDateTime

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/performances")
class AdminPerformanceController(
    private val adminPerformanceService: AdminPerformanceService,
    private val seatService: SeatService,
    private val bookService: BookService
) {

    @GetMapping
    fun viewPerformancesForAdmin(
        session: HttpSession,
        @RequestParam(value = "page", defaultValue = "0", required = false) page: Int,
        @RequestParam(value = "size", defaultValue = "5", required = false) size: Int,
        model: Model
    ): String {
        val pageable: Pageable = PageRequest.of(page, size)
        return renderPerformanceList(
            session,
            model,
            adminPerformanceService.getPerformancesSortedByLatestForAdmin(pageable),
            page,
            "?size=$size"
        )
    }

    @GetMapping(params = ["search"])
    fun searchPerformances(
        session: HttpSession,
        @RequestParam(value = "search", defaultValue = "", required = false) keyword: String?,
        @RequestParam(value = "page", defaultValue = "0", required = false) page: Int,
        @RequestParam(value = "size", defaultValue = "5", required = false) size: Int,
        model: Model
    ): String {
        val pageable: Pageable = PageRequest.of(page, size)

        if (keyword.isNullOrEmpty()) {
            viewPerformancesForAdmin(session, page, size, model)
        }

        return renderPerformanceList(
            session,
            model,
            adminPerformanceService.searchPerformancesByKeyword(keyword, pageable),
            page,
            "?search=$keyword&size=$size"
        )
    }


    @GetMapping(params = ["sort"])
    fun viewPerformancesSortedByForAdmin(
        session: HttpSession,
        @RequestParam(value = "sort", defaultValue = "latest", required = false) sort: String,
        @RequestParam(value = "page", defaultValue = "0", required = false) page: Int,
        @RequestParam(value = "size", defaultValue = "5", required = false) size: Int,
        model: Model
    ): String {
        val pageable: Pageable = PageRequest.of(page, size)

        return renderPerformanceList(
            session,
            model,
            getPerformancesBySortForAdmin(sort, pageable),
            page,
            "?sort=$sort&size=$size"
        )
    }

    private fun getPerformancesBySortForAdmin(
        sort: String,
        pageable: Pageable
    ): Page<AdminPerformanceResponseDto> {
        return when (sort) {
            "likes" -> adminPerformanceService.getPerformancesSortedByLikesForAdmin(pageable)
            "deleted" -> adminPerformanceService.getPerformancesSortedByDeletedForAdmin(pageable)
            else -> adminPerformanceService.getPerformancesSortedByLatestForAdmin(pageable)
        }
    }

    @GetMapping(params = ["startDate", "endDate"])
    fun viewPerformancesWithinPeriodForAdmin(
        session: HttpSession,
        @RequestParam(value = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam(value = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime,
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "5") size: Int,
        model: Model
    ): String {
        periodValidator(startDate, endDate)
        val pageable: Pageable = PageRequest.of(page, size)

        return renderPerformanceList(
            session,
            model,
            adminPerformanceService.getPerformancesByReservationPeriodForAdmin(
                startDate,
                endDate, pageable
            ),
            page,
            "?startDate=$startDate&endDate=$endDate&size=$size"
        )
    }

    @GetMapping(params = ["category"])
    fun viewPerformancesByCategoryForAdmin(
        session: HttpSession,
        @RequestParam(value = "category") category: Category,
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "5") size: Int,
        model: Model
    ): String {
        val pageable: Pageable = PageRequest.of(page, size)

        return renderPerformanceList(
            session,
            model,
            adminPerformanceService.getPerformancesByCategoryForAdmin(category, pageable),
            page,
            "?category=$category&size=$size"
        )
    }

    @GetMapping("/{performanceId}")
    fun getPerformanceDetailForAdmin(
        @PathVariable performanceId: Long,
        session: HttpSession,
        model: Model
    ): String {
        val loginUser = validateSessionUser(session)

        val performanceDetail = adminPerformanceService.getPerformanceDetailForAdmin(
            performanceId
        )

        val seatViewDtos = seatService.getSeatsForPerformance(performanceId)

        model.addAttribute("performanceDetail", performanceDetail)
        model.addAttribute("performanceId", performanceId)
        model.addAttribute("userId", loginUser.id)
        model.addAttribute("seats", seatViewDtos)
        model.addAttribute("showBackButton", true)

        return "admin/performance_detail_admin"
    }

    @GetMapping("/create")
    fun createPerformance(model: Model): String {
        val places = adminPerformanceService.allPlaces
        model.addAttribute("places", places)
        model.addAttribute("showBackButton", true)
        return "admin/create_performance"
    }

    @GetMapping("/update/{performanceId}")
    fun updatePerformance(@PathVariable("performanceId") id: Long, model: Model): String {
        val performance = adminPerformanceService.getPerformanceUpdateForAdmin(
            id
        )
        val places = adminPerformanceService.allPlaces
        model.addAttribute("showBackButton", true)
        model.addAttribute("performance", performance)
        model.addAttribute("places", places)
        return "admin/update_performance"
    }

    @GetMapping("/chart")
    fun adminChart(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "3") size: Int,
        model: Model
    ): String {
        val pageable: Pageable = PageRequest.of(page, size)
        val statics = adminPerformanceService.getPerformanceStatics(
            pageable
        )
        model.addAttribute("stats", statics.content)
        model.addAttribute("currentPage", statics.number)
        model.addAttribute("totalPages", statics.totalPages)
        return "admin/chart_admin"
    }

    @GetMapping("/book/{performanceId}")
    fun viewAdminPerformanceBookDetail(
        @PathVariable("performanceId") id: Long,
        @PageableDefault(size = 5) pageable: Pageable, model: Model
    ): String {
        val performanceBookDetail = adminPerformanceService.getPerformanceBookDetail(
            id
        )
        val bookAdminList = bookService.getBookAdminList(id, pageable)
        model.addAttribute("showBackButton", true)
        model.addAttribute("performanceBookDetail", performanceBookDetail)
        model.addAttribute("bookAdminListPage", bookAdminList)
        return "admin/performance_book_detail_admin"
    }

    @GetMapping("/book/{performanceId}/{bookId}")
    fun viewAdminPerformanceBookUserDetail(
        @PathVariable("performanceId") performanceId: Long, @PathVariable("bookId") bookId: Long,
        model: Model
    ): String {
        val bookUserDetail = bookService.getBookUserDetail(bookId, performanceId)
        model.addAttribute("showBackButton", true)
        model.addAttribute("bookUserDetail", bookUserDetail)
        return "admin/performance_book_user_detail_admin"
    }

    private fun renderPerformanceList(
        session: HttpSession,
        model: Model,
        performances: Page<AdminPerformanceResponseDto>,
        page: Int,
        queryParams: String
    ): String {
        val loginUser = validateSessionUser(session)
        val userId = loginUser.id

        model.addAttribute("userId", userId)
        model.addAttribute("performances", performances.content)
        model.addAttribute("categories", Category.entries.toTypedArray())
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPage", performances.totalPages)
        model.addAttribute("baseQuery", queryParams)

        return "/admin/view_performances_admin"
    }
}