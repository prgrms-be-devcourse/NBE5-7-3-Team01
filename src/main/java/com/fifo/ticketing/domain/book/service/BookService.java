package com.fifo.ticketing.domain.book.service;

import static com.fifo.ticketing.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static com.fifo.ticketing.global.exception.ErrorCode.NOT_FOUND_PERFORMANCE;

import com.fifo.ticketing.domain.book.dto.BookAdminDetailDto;
import com.fifo.ticketing.domain.book.dto.BookCompleteDto;
import com.fifo.ticketing.domain.book.dto.BookCreateRequest;
import com.fifo.ticketing.domain.book.dto.BookMailSendDto;
import com.fifo.ticketing.domain.book.dto.BookUserDetailDto;
import com.fifo.ticketing.domain.book.dto.BookedView;
import com.fifo.ticketing.domain.book.entity.Book;
import com.fifo.ticketing.domain.book.entity.BookSeat;
import com.fifo.ticketing.domain.book.entity.BookStatus;
import com.fifo.ticketing.domain.book.mapper.BookMapper;
import com.fifo.ticketing.domain.book.repository.BookRepository;
import com.fifo.ticketing.domain.book.repository.BookSeatRepository;
import com.fifo.ticketing.domain.performance.entity.Performance;
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository;
import com.fifo.ticketing.domain.seat.entity.Seat;
import com.fifo.ticketing.domain.seat.entity.SeatStatus;
import com.fifo.ticketing.domain.seat.service.SeatService;
import com.fifo.ticketing.domain.user.entity.User;
import com.fifo.ticketing.domain.user.repository.UserRepository;
import com.fifo.ticketing.global.exception.ErrorCode;
import com.fifo.ticketing.global.exception.ErrorException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    @Value("${file.url-prefix}")
    private String urlPrefix;

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final PerformanceRepository performanceRepository;
    private final SeatService seatService;
    private final BookSeatRepository bookSeatRepository;
    private final BookScheduleManager bookScheduleManager;

    @Transactional
    public Long createBook(Long performanceId, Long userId, BookCreateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ErrorException(NOT_FOUND_MEMBER));

        Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(() -> new ErrorException(NOT_FOUND_PERFORMANCE));

        List<Seat> selectedSeats = seatService.validateBookSeats(request.getSeatIds());

        int totalPrice = selectedSeats.stream().mapToInt(Seat::getPrice).sum();
        int quantity = selectedSeats.size();

        Book book = saveBookAndBookSeats(user, performance, totalPrice, quantity, selectedSeats);

        scheduleBookCancel(book.getId());

        return book.getId();
    }

    private void scheduleBookCancel(Long bookId) {
        LocalDateTime runTime = LocalDateTime.now().plusMinutes(10);
        bookScheduleManager.scheduleCancelTask(bookId, runTime);
    }

    private Book saveBookAndBookSeats(User user, Performance performance, int totalPrice,
        int quantity,
        List<Seat> selectedSeats) {
        Book book = BookMapper.toBookEntity(user, performance, totalPrice, quantity);
        bookRepository.save(book);
        bookRepository.flush();

        List<BookSeat> bookSeatList = BookMapper.toBookSeatEntities(book, selectedSeats);

        bookSeatRepository.saveAll(bookSeatList);
        return book;
    }

    @Transactional
    public BookCompleteDto getBookCompleteInfo(Long bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_BOOK));

        return BookMapper.toBookCompleteDto(book, urlPrefix);
    }

    @Transactional
    public void completePayment(Long bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_BOOK));

        List<BookSeat> bookSeats = bookSeatRepository.findAllByBookId(book.getId());

        book.payed();

        SeatService.changeSeatStatus(bookSeats, SeatStatus.OCCUPIED);

    }


    @Transactional
    public List<Book> cancelAllBook(Performance performance) {
        bookRepository.cancelAllByPerformance(performance, BookStatus.ADMIN_REFUNDED,
            BookStatus.PAYED);
        return bookRepository.findAllWithUserAndPerformanceByPerformanceAndBookStatus(performance,
            BookStatus.ADMIN_REFUNDED);
    }

    public Page<BookedView> getBookedList(Long userId, String title, BookStatus status,
        Pageable pageable) {
        Page<Book> bookPage;
        if (title != null && status != null) {
            bookPage = bookRepository.findAllByUserIdAndTitleAndBookStatus(
                userId, title, status, pageable);
        } else if (title != null) {
            bookPage = bookRepository.findAllByUserIdAndTitle(userId, title,
                pageable);
        } else if (status != null) {
            bookPage = bookRepository.findAllByUserIdAndBookStatus(userId, status,
                pageable);
        } else {
            bookPage = bookRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return BookMapper.toBookedViewDtoList(bookPage, urlPrefix);
    }

    @Transactional
    public BookMailSendDto getBookMailInfo(Long bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_BOOK));

        return BookMapper.getBookMailInfo(book);
    }


    @Transactional(readOnly = true)
    public Page<BookAdminDetailDto> getBookAdminList(Long performanceId, Pageable pageable) {
        Page<BookAdminDetailDto> allBookDetailsAdmin = bookRepository.findAllBookDetailsAdmin(
            performanceId, pageable);
        return Objects.requireNonNullElseGet(allBookDetailsAdmin, Page::empty);
    }

    @Transactional(readOnly = true)
    public BookUserDetailDto getBookUserDetail(Long bookId, Long performanceId) {
        BookUserDetailDto bookDetailByBookId = bookRepository.findBookDetailByBookId(bookId,
            performanceId);
        if (bookDetailByBookId == null) {
            throw new ErrorException(ErrorCode.NOT_FOUND_BOOK);
        } else {
            bookDetailByBookId.setUrlPrefix(urlPrefix);
            return bookDetailByBookId;
        }
    }

    @Transactional
    public void cancelBookByAdmin(Long bookId) {
        Book findBook = bookRepository.findById(bookId).orElseThrow(() -> new ErrorException(
            ErrorCode.NOT_FOUND_BOOK)
        );
        findBook.canceled();

        List<BookSeat> bookSeats = bookSeatRepository.findAllByBookId(bookId);

        SeatService.changeSeatStatus(bookSeats, SeatStatus.AVAILABLE);
    }

}

