package com.fifo.ticketing.domain.performance.controller.view

import com.fifo.ticketing.domain.book.dto.BookSeatViewDto
import com.fifo.ticketing.domain.like.service.LikeService
import com.fifo.ticketing.domain.performance.dto.PerformanceDetailResponse
import com.fifo.ticketing.domain.performance.dto.PerformanceResponseDto
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.service.PerformanceService
import com.fifo.ticketing.domain.seat.service.SeatService
import com.fifo.ticketing.domain.user.dto.SessionUser
import com.fifo.ticketing.global.util.DateTimeValidator.periodValidator
import com.fifo.ticketing.global.util.UserValidator.validateSessionUser
import jakarta.servlet.http.HttpSession
import org.apache.commons.lang3.StringUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDateTime


@Controller
@RequestMapping("/performances")
class PerformanceController(
    private val performanceService: PerformanceService,
    private val seatService: SeatService,
    private val likeService: LikeService
) {

    @GetMapping
    fun viewPerformances(
        session: HttpSession,
        @RequestParam(value = "page", defaultValue = "0", required = false) page: Int,
        @RequestParam(value = "size", defaultValue = "5", required = false) size: Int,
        model: Model
    ): String {
        val pageable = PageRequest.of(page, size)
        val performances = performanceService.getPerformancesSortedByLatest(pageable)
        val queryParams = "size=$size"
        return renderPerformanceList(session, model, performances, page, queryParams)
    }

    @GetMapping(params = ["search"])
    fun searchPerformances(
        session: HttpSession,
        @RequestParam(value = "search", defaultValue = "", required = false) keyword: String,
        @RequestParam(value = "page", defaultValue = "0", required = false) page: Int,
        @RequestParam(value = "size", defaultValue = "5", required = false) size: Int,
        model: Model
    ): String {
        if (StringUtils.isBlank(keyword)) {
            return viewPerformances(session, page, size, model)
        }
        val pageable = PageRequest.of(page, size)
        val performances = performanceService.searchPerformancesByKeyword(keyword, pageable)
        val queryParams = "search=$keyword&size=$size"
        return renderPerformanceList(session, model, performances, page, queryParams)
    }

    @GetMapping(params = ["sort"])
    fun viewPerformancesSortedBy(
        session: HttpSession,
        @RequestParam(value = "sort", defaultValue = "latest", required = false) sort: String,
        @RequestParam(value = "page", defaultValue = "0", required = false) page: Int,
        @RequestParam(value = "size", defaultValue = "5", required = false) size: Int,
        model: Model
    ): String {
        val pageable = PageRequest.of(page, size)
        val performances = getPerformancesBySort(sort, pageable)
        val queryParams = "sort=$sort&size=$size"
        return renderPerformanceList(session, model, performances, page, queryParams)
    }

    private fun getPerformancesBySort(
        sort: String,
        pageable: Pageable
    ): Page<PerformanceResponseDto> {
        return if (StringUtils.equals(sort, "likes")) {
            performanceService.getPerformancesSortedByLikes(pageable)
        } else {
            performanceService.getPerformancesSortedByLatest(pageable)
        }
    }

    @GetMapping(params = ["startDate", "endDate"])
    fun viewPerformancesWithinPeriod(
        session: HttpSession,
        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime,
        @RequestParam(value = "page", defaultValue = "0", required = false) page: Int,
        @RequestParam(value = "size", defaultValue = "5", required = false) size: Int,
        model: Model
    ): String {
        periodValidator(startDate, endDate)
        val pageable = PageRequest.of(page, size)
        val performances =
            performanceService.getPerformancesByReservationPeriod(startDate, endDate, pageable)
        val queryParams = "startDate=$startDate&endDate=$endDate&size=$size"
        return renderPerformanceList(session, model, performances, page, queryParams)
    }

    @GetMapping(params = ["category"])
    fun viewPerformancesByCategory(
        session: HttpSession,
        @RequestParam("category") category: Category,
        @RequestParam(value = "page", defaultValue = "0", required = false) page: Int,
        @RequestParam(value = "size", defaultValue = "5", required = false) size: Int,
        model: Model
    ): String {
        val pageable = PageRequest.of(page, size)
        val performances = performanceService.getPerformancesByCategory(category, pageable)
        val queryParams = "category=$category&size=$size"
        return renderPerformanceList(session, model, performances, page, queryParams)
    }

    @GetMapping("/{performanceId}")
    fun getPerformanceDetail(
        @PathVariable performanceId: Long,
        session: HttpSession,
        model: Model
    ): String {
        val loginUser: SessionUser = validateSessionUser(session)
        val userId = loginUser.id
        val performanceDetail: PerformanceDetailResponse =
            performanceService.getPerformanceDetail(performanceId)
        val seatViewDtos: List<BookSeatViewDto> = seatService.getSeatsForPerformance(performanceId)
        val likedPerformanceIds = likeService.getLikedPerformancesIds(userId)

        model.addAttribute("userId", userId)
        model.addAttribute("likedPerformanceIds", likedPerformanceIds)
        model.addAttribute("performanceDetail", performanceDetail)
        model.addAttribute("performanceId", performanceId)
        model.addAttribute("seats", seatViewDtos)

        return "performance/detail"
    }

    private fun renderPerformanceList(
        session: HttpSession,
        model: Model,
        performances: Page<PerformanceResponseDto>,
        page: Int,
        queryParams: String
    ): String {
        val loginUser: SessionUser = validateSessionUser(session)
        val userId = loginUser.id
        val likedPerformanceIds = likeService.getLikedPerformancesIds(userId)

        model.addAttribute("userId", userId)
        model.addAttribute("performances", performances.content)
        model.addAttribute("categories", Category.entries.toTypedArray())
        model.addAttribute("likedPerformanceIds", likedPerformanceIds)
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPage", performances.totalPages)
        model.addAttribute("baseQuery", queryParams)

        return "performance/view_performances"
    }
}