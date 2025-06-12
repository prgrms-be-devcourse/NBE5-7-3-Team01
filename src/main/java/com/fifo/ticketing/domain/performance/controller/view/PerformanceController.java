package com.fifo.ticketing.domain.performance.controller.view;


import com.fifo.ticketing.domain.book.dto.BookSeatViewDto;
import com.fifo.ticketing.domain.like.service.LikeService;
import com.fifo.ticketing.domain.performance.dto.PerformanceDetailResponse;
import com.fifo.ticketing.domain.performance.dto.PerformanceResponseDto;
import com.fifo.ticketing.domain.performance.entity.Category;
import com.fifo.ticketing.domain.performance.service.PerformanceService;
import com.fifo.ticketing.domain.seat.service.SeatService;
import com.fifo.ticketing.domain.user.dto.SessionUser;
import com.fifo.ticketing.global.util.DateTimeValidator;
import com.fifo.ticketing.global.util.UserValidator;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/performances")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final SeatService seatService;
    private final LikeService likeService;

    @GetMapping
    public String viewPerformances(
        HttpSession session,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "5", required = false) int size,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PerformanceResponseDto> performances = performanceService.getPerformancesSortedByLatest(
            pageable);
        String queryParams = "size=" + size;

        return renderPerformanceList(session, model, performances, page, queryParams);
    }

    @GetMapping(params = {"search"})
    public String searchPerformances(
        HttpSession session,
        @RequestParam(value = "search", defaultValue = "", required = false) String keyword,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "5", required = false) int size,
        Model model
    ) {
        if (StringUtils.isBlank(keyword)) {
            return viewPerformances(session, page, size, model);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<PerformanceResponseDto> performances = performanceService.searchPerformancesByKeyword(
            keyword, pageable);
        String queryParams = "search=" + keyword + "&size=" + size;

        return renderPerformanceList(session, model, performances, page, queryParams);
    }

    @GetMapping(params = {"sort"})
    public String viewPerformancesSortedBy(
        HttpSession session,
        @RequestParam(value = "sort", defaultValue = "latest", required = false) String sort,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "5", required = false) int size,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PerformanceResponseDto> performances = getPerformancesBySort(sort, pageable);
        String queryParams = "sort=" + sort + "&size=" + size;

        return renderPerformanceList(session, model, performances, page, queryParams);
    }

    private Page<PerformanceResponseDto> getPerformancesBySort(String sort, Pageable pageable) {
        if (StringUtils.equals(sort, "likes")) {
            return performanceService.getPerformancesSortedByLikes(pageable);
        }
        return performanceService.getPerformancesSortedByLatest(pageable);
    }

    @GetMapping(params = {"startDate", "endDate"})
    public String viewPerformancesWithinPeriod(
        HttpSession session,
        @RequestParam(value = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(value = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "5", required = false) int size,
        Model model
    ) {
        DateTimeValidator.periodValidator(startDate, endDate);
        Pageable pageable = PageRequest.of(page, size);
        Page<PerformanceResponseDto> performances = performanceService.getPerformancesByReservationPeriod(
            startDate, endDate, pageable);
        String queryParams = "startDate=" + startDate + "&endDate=" + endDate + "&size=" + size;

        return renderPerformanceList(session, model, performances, page, queryParams);
    }

    @GetMapping(params = "category")
    public String viewPerformancesByCategory(
        HttpSession session,
        @RequestParam(value = "category") Category category,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "5", required = false) int size,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PerformanceResponseDto> performances = performanceService.getPerformancesByCategory(
            category, pageable);
        String queryParams = "category=" + category + "&size=" + size;

        return renderPerformanceList(session, model, performances, page, queryParams);
    }

    @GetMapping("/{performanceId}")
    public String getPerformanceDetail(
        @PathVariable(value = "performanceId") Long performanceId,
        HttpSession session,
        Model model
    ) {
        SessionUser loginUser = UserValidator.validateSessionUser(session);
        Long userId = loginUser.id;

        PerformanceDetailResponse performanceDetail = performanceService.getPerformanceDetail(
            performanceId);

        List<BookSeatViewDto> seatViewDtos = seatService.getSeatsForPerformance(performanceId);

        List<Long> likedPerformanceIds = likeService.getLikedPerformancesIds(userId);

        model.addAttribute("userId", userId);
        model.addAttribute("likedPerformanceIds", likedPerformanceIds);
        model.addAttribute("performanceDetail", performanceDetail);
        model.addAttribute("performanceId", performanceId);
        model.addAttribute("seats", seatViewDtos);

        return "performance/detail";
    }

    private String renderPerformanceList(HttpSession session, Model model,
        Page<PerformanceResponseDto> performances, int page, String queryParams) {
        SessionUser loginUser = UserValidator.validateSessionUser(session);
        Long userId = loginUser.id;
        List<Long> likedPerformanceIds = likeService.getLikedPerformancesIds(userId);

        model.addAttribute("userId", userId);
        model.addAttribute("performances", performances.getContent());
        model.addAttribute("categories", Category.values());
        model.addAttribute("likedPerformanceIds", likedPerformanceIds);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", performances.getTotalPages());
        model.addAttribute("baseQuery", queryParams);

        return "/performance/view_performances";
    }
}