package com.fifo.ticketing.domain.book.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import com.fifo.ticketing.domain.book.dto.BookCompleteDto;
import com.fifo.ticketing.domain.book.dto.BookMailSendDto;
import com.fifo.ticketing.domain.book.dto.BookUserDetailDto;
import com.fifo.ticketing.domain.book.dto.BookedView;
import com.fifo.ticketing.domain.book.entity.Book;
import com.fifo.ticketing.domain.book.entity.BookSeat;
import com.fifo.ticketing.domain.book.entity.BookStatus;
import com.fifo.ticketing.domain.book.repository.BookRepository;
import com.fifo.ticketing.domain.book.repository.BookSeatRepository;
import com.fifo.ticketing.domain.performance.entity.Category;
import com.fifo.ticketing.domain.performance.entity.Grade;
import com.fifo.ticketing.domain.performance.entity.Performance;
import com.fifo.ticketing.domain.performance.entity.Place;
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository;
import com.fifo.ticketing.domain.seat.entity.Seat;
import com.fifo.ticketing.domain.seat.entity.SeatStatus;
import com.fifo.ticketing.domain.seat.service.SeatService;
import com.fifo.ticketing.domain.user.entity.User;
import com.fifo.ticketing.domain.user.repository.UserRepository;
import com.fifo.ticketing.global.entity.File;
import com.fifo.ticketing.global.exception.ErrorCode;
import com.fifo.ticketing.global.exception.ErrorException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;


@ActiveProfiles("ci")
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PerformanceRepository performanceRepository;
    @Mock
    private SeatService seatService;
    @Mock
    private BookSeatRepository bookSeatRepository;
    @Mock
    private BookScheduleManager bookScheduleManager;
    private Place place;
    private Performance mockPerformance;
    private User mockUser;
    private Book mockBook;
    private Seat mockSeat;
    private Grade modckGrade;
    private BookSeat mockBookSeat;

    private Pageable pageable;

    private String urlPrefix;

    @BeforeEach
    void setUp() {
        urlPrefix = "https://picsum.photos/200";
        ReflectionTestUtils.setField(bookService, "urlPrefix", urlPrefix);

        mockUser = User.builder()
            .id(1L)
            .email("example@gmail.com")
            .password("123")
            .username("테스트 유저")
            .build();

        place = new Place(1L, "서울특별시 서초구 서초동 1307", "강남아트홀", 100);
        File mockFile = File.builder()
            .id(1L)
            .encodedFileName("poster.jpg")
            .originalFileName("sample.jpg")
            .build();

        mockPerformance = Performance.builder()
            .id(1L)
            .title("라따뚜이")
            .description("라따뚜이는 픽사의 영화입니다.")
            .place(place)
            .startTime(LocalDateTime.of(2025, 6, 1, 19, 0))
            .endTime(LocalDateTime.of(2025, 6, 1, 21, 0))
            .category(Category.MOVIE)
            .performanceStatus(false)
            .deletedFlag(false)
            .reservationStartTime(LocalDateTime.of(2025, 5, 12, 19, 0))
            .file(mockFile)
            .build();

        mockBook = Book.builder()
            .id(1L)
            .performance(mockPerformance)
            .user(mockUser)
            .totalPrice(20000)
            .quantity(2)
            .bookStatus(BookStatus.CONFIRMED)
            .build();

        modckGrade = Grade.builder()
            .id(1L)
            .grade("A")
            .place(place)
            .defaultPrice(5000)
            .seatCount(10)
            .build();

        mockSeat = new Seat(1L, mockPerformance, "A1", 5000, modckGrade, SeatStatus.BOOKED);

        mockBookSeat = BookSeat.builder()
            .id(1L)
            .book(mockBook)
            .seat(mockSeat)
            .build();

        pageable = PageRequest.of(0, 5);
    }

    @Test
    @DisplayName("cancelBookByAdmin_성공")
    void cancelBookByAdmin_success() {
        // given
        Long bookId = mockBook.getId();
        given(bookRepository.findById(bookId)).willReturn(Optional.of(mockBook));
        given(bookSeatRepository.findAllByBookId(bookId)).willReturn(List.of(mockBookSeat));

        // when
        bookService.cancelBookByAdmin(bookId);

        // then
        assertEquals(BookStatus.CANCELED, mockBook.getBookStatus());
        verify(bookRepository).findById(bookId);
        verify(bookSeatRepository).findAllByBookId(bookId);
    }

    @Test
    @DisplayName("cancelBookByAdmin_실패")
    void cancelBookByAdmin_fail() {
        // given
        Long bookId = 10L;
        given(bookRepository.findById(bookId)).willReturn(Optional.empty());

        // when & then
        assertThrows(ErrorException.class, () -> bookService.cancelBookByAdmin(bookId));
    }

    @Test
    @DisplayName("getBookUserDetail_성공")
    void getBookUserDetail_success() {
        // given
        BookUserDetailDto mockDto = new BookUserDetailDto(
            1L,
            1L,
            "라따뚜이",
            20000,
            2,
            "홍길동",
            "poster.jpg",
            BookStatus.CONFIRMED
        );
        mockDto.setUrlPrefix("https://picsum.photos/200");

        given(bookRepository.findBookDetailByBookId(mockBook.getId(), mockPerformance.getId()))
            .willReturn(mockDto);

        // when
        BookUserDetailDto result = bookService.getBookUserDetail(mockBook.getId(),
            mockPerformance.getId());

        // then
        assertNotNull(result);
        assertEquals("https://picsum.photos/200", result.getUrlPrefix());
        assertEquals(2, result.getQuantity());
    }

    @Test
    @DisplayName("getBookUserDetail_실패")
    void getBookUserDetail_fail() {
        // given
        Long bookId = 10L;
        Long performanceId = 10L;

        given(bookRepository.findBookDetailByBookId(bookId, performanceId))
            .willReturn(null);

        // when & then
        ErrorException exception = assertThrows(ErrorException.class, () -> {
            bookService.getBookUserDetail(bookId, performanceId);
        });

        assertEquals(ErrorCode.NOT_FOUND_BOOK, exception.getErrorCode());
        verify(bookRepository).findBookDetailByBookId(bookId, performanceId);
    }


    @Test
    @DisplayName("getBookMailInfo_성공")
    void getBookMailInfo_success() {
        // given
        Long bookId = 1L;
        given(bookRepository.findById(bookId)).willReturn(Optional.of(mockBook));

        // when
        BookMailSendDto result = bookService.getBookMailInfo(bookId);

        // then
        assertNotNull(result);
        verify(bookRepository).findById(bookId);
    }

    @Test
    @DisplayName("getBookMailInfo_실패")
    void getBookMailInfo_fail() {
        // given
        Long bookId = 10L;
        given(bookRepository.findById(bookId)).willReturn(Optional.empty());

        // when & then
        assertThrows(ErrorException.class, () -> bookService.getBookMailInfo(bookId));
        verify(bookRepository).findById(bookId);
    }

    @Test
    @DisplayName("getBookedList_titleAndStatus_성공")
    void getBookedList_titleAndStatus_success() {
        // given
        String title = "라따뚜이";
        BookStatus status = BookStatus.CONFIRMED;

        Page<Book> bookPage = new PageImpl<>(List.of(mockBook));
        given(bookRepository.findAllByUserIdAndTitleAndBookStatus(mockUser.getId(), title, status,
            pageable))
            .willReturn(bookPage);

        // when & then
        Page<BookedView> result = bookService.getBookedList(mockUser.getId(), title, status,
            pageable);

        assertEquals(1, result.getContent().size());
        verify(bookRepository).findAllByUserIdAndTitleAndBookStatus(mockUser.getId(), title, status,
            pageable);
    }


    @Test
    @DisplayName("getBookedList_title_성공")
    void getBookedList_title_success() {
        // given
        String title = "라따뚜이";

        Page<Book> bookPage = new PageImpl<>(List.of(mockBook));
        given(bookRepository.findAllByUserIdAndTitle(mockUser.getId(), title, pageable))
            .willReturn(bookPage);

        // when & then
        Page<BookedView> result = bookService.getBookedList(mockUser.getId(), title, null,
            pageable);

        assertEquals(1, result.getContent().size());
        verify(bookRepository).findAllByUserIdAndTitle(mockUser.getId(), title, pageable);
    }

    @Test
    @DisplayName("getBookedList_status_성공")
    void getBookedList_status_success() {
        // given
        BookStatus status = BookStatus.PAYED;

        Page<Book> bookPage = new PageImpl<>(List.of(mockBook));
        given(bookRepository.findAllByUserIdAndBookStatus(mockUser.getId(), status, pageable))
            .willReturn(bookPage);

        // when & then
        Page<BookedView> result = bookService.getBookedList(mockUser.getId(), null, status,
            pageable);

        assertEquals(1, result.getContent().size());
        verify(bookRepository).findAllByUserIdAndBookStatus(mockUser.getId(), status, pageable);
    }

    @Test
    @DisplayName("getBookedList_null_성공")
    void getBookedList_null_success() {
        // given
        Page<Book> bookPage = new PageImpl<>(List.of(mockBook));
        given(bookRepository.findAllByUserIdOrderByCreatedAtDesc(mockUser.getId(), pageable))
            .willReturn(bookPage);

        // when & then
        Page<BookedView> result = bookService.getBookedList(mockUser.getId(), null, null, pageable);

        assertEquals(1, result.getContent().size());
        verify(bookRepository).findAllByUserIdOrderByCreatedAtDesc(mockUser.getId(), pageable);
    }

    @Test
    @DisplayName("getBookedList_실패")
    void getBookedList_fail() {
        // given
        given(bookRepository.findAllByUserIdOrderByCreatedAtDesc(mockUser.getId(), pageable))
            .willThrow(new RuntimeException("DB error"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            bookService.getBookedList(mockUser.getId(), null, null, pageable);
        });

        verify(bookRepository).findAllByUserIdOrderByCreatedAtDesc(mockUser.getId(), pageable);
    }


    @Test
    @DisplayName("cancelAllBook_성공")
    void cancelAllBook_success() {
        // given
        Performance performance = mockPerformance;
        List<Book> books = List.of(mockBook);

        doNothing().when(bookRepository).cancelAllByPerformance(
            performance, BookStatus.ADMIN_REFUNDED, BookStatus.PAYED);

        given(bookRepository.findAllWithUserAndPerformanceByPerformanceAndBookStatus(
            performance, BookStatus.ADMIN_REFUNDED
        )).willReturn(books);

        // when
        List<Book> result = bookService.cancelAllBook(performance);

        // then
        assertEquals(1, result.size());
        assertEquals(mockBook, result.get(0));

        verify(bookRepository).cancelAllByPerformance(performance, BookStatus.ADMIN_REFUNDED,
            BookStatus.PAYED);
        verify(bookRepository).findAllWithUserAndPerformanceByPerformanceAndBookStatus(
            performance,
            BookStatus.ADMIN_REFUNDED
        );
    }

    @Test
    @DisplayName("cancelAllBook_update_실패")
    void cancelAllBook_fail_update() {
        // given
        Performance performance = mockPerformance;

        doThrow(new RuntimeException("DB update 실패"))
            .when(bookRepository)
            .cancelAllByPerformance(performance, BookStatus.ADMIN_REFUNDED, BookStatus.PAYED);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookService.cancelAllBook(performance);
        });

        assertEquals("DB update 실패", exception.getMessage());

        verify(bookRepository).cancelAllByPerformance(performance, BookStatus.ADMIN_REFUNDED,
            BookStatus.PAYED);
        verify(bookRepository, never()).findAllWithUserAndPerformanceByPerformanceAndBookStatus(
            any(), any());
    }

    @Test
    @DisplayName("cancelAllBook_조회_실패")
    void cancelAllBook_fail_find() {
        // given
        Performance performance = mockPerformance;

        doNothing().when(bookRepository).cancelAllByPerformance(
            performance, BookStatus.ADMIN_REFUNDED, BookStatus.PAYED);

        given(bookRepository.findAllWithUserAndPerformanceByPerformanceAndBookStatus(performance,
            BookStatus.ADMIN_REFUNDED))
            .willThrow(new RuntimeException("조회 실패"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookService.cancelAllBook(performance);
        });

        assertEquals("조회 실패", exception.getMessage());

        verify(bookRepository).cancelAllByPerformance(performance, BookStatus.ADMIN_REFUNDED,
            BookStatus.PAYED);
        verify(bookRepository).findAllWithUserAndPerformanceByPerformanceAndBookStatus(performance,
            BookStatus.ADMIN_REFUNDED);
    }


    @Test
    @DisplayName("completePayment_성공")
    void completePayment_success() throws Exception {
        // given
        Long bookId = 1L;
        given(bookRepository.findById(bookId)).willReturn(Optional.of(mockBook));
        given(bookSeatRepository.findAllByBookId(bookId)).willReturn(List.of(mockBookSeat));

        // when
        bookService.completePayment(bookId);

        // then
        assertEquals(BookStatus.PAYED, mockBook.getBookStatus());
        verify(bookRepository).findById(bookId);
        verify(bookSeatRepository).findAllByBookId(bookId);
    }

    @Test
    @DisplayName("completePayment_실패")
    void completePayment_fail() throws Exception {
        // given
        Long bookId = 10L;
        given(bookRepository.findById(bookId)).willReturn(Optional.empty());

        // when & then
        ErrorException errorException = assertThrows(ErrorException.class, () -> {
            bookService.completePayment(bookId);
        });
        assertEquals(ErrorCode.NOT_FOUND_BOOK, errorException.getErrorCode());

        assertEquals(BookStatus.CONFIRMED, mockBook.getBookStatus());
    }

    @Test
    @DisplayName("getBookCompleteInfo_성공")
    void getBookCompleteInfo_success() throws Exception {
        // given
        Long bookId = 1L;
        given(bookRepository.findById(bookId)).willReturn(Optional.of(mockBook));

        // when
        BookCompleteDto result = bookService.getBookCompleteInfo(bookId);

        // then
        assertNotNull(result);
        assertEquals(mockBook.getPerformance().getId(), result.getPerformanceId());
        assertEquals(mockBook.getPerformance().getTitle(), result.getPerformanceTitle());
        assertEquals(mockBook.getTotalPrice(), result.getTotalPrice());
        assertEquals(mockBook.getQuantity(), result.getQuantity());
        assertEquals("https://picsum.photos/200" + result.getEncodedFileName(), result.getUrl());
    }

    @Test
    @DisplayName("getBookCompleteInfo_실패")
    void getBookCompleteInfo_fail() throws Exception {
        // given
        Long bookId = 10L;
        given(bookRepository.findById(bookId)).willReturn(Optional.empty());

        // when & then
        ErrorException exception = assertThrows(ErrorException.class, () -> {
            bookService.getBookCompleteInfo(bookId);
        });

        assertEquals(ErrorCode.NOT_FOUND_BOOK, exception.getErrorCode());
        verify(bookRepository).findById(bookId);
    }
}