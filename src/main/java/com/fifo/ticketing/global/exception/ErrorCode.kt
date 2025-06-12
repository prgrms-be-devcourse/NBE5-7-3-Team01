package com.fifo.ticketing.global.exception

enum class ErrorCode(
    val code: String,
    val message: String,
    val errorStatus: ErrorStatus
) {

    NOT_FOUND_MEMBER("AUTH-001", "존재하지 않는 회원입니다.", ErrorStatus.NOT_FOUND),
    NOT_FOUND_PERFORMANCES("PERFORMANCE-001", "예매 가능한 공연이 존재하지 않습니다.", ErrorStatus.NOT_FOUND),
    NOT_FOUND_PERFORMANCE("PERFORMANCE-002", "존재하지 않는 공연입니다.", ErrorStatus.NOT_FOUND),
    ADMIN_NOT_FOUND_PERFORMANCES("PERFORMANCE-003", "조회 가능한 공연이 없습니다.", ErrorStatus.NOT_FOUND),
    INVALID_DELETED_PERFORMANCE("PERFORMANCE-004", "삭제된 공연입니다.", ErrorStatus.BAD_REQUEST),
    SEAT_ALREADY_BOOKED("SEAT-001", "해당 좌석은 이미 예약되었습니다.", ErrorStatus.CONFLICT),
    FILE_UPLOAD_FAILED("FILE-001", "파일 업로드에 실패하였습니다.", ErrorStatus.INTERNAL_SERVER_ERROR),
    FILE_UPDATE_FAILED("FILE-002", "파일 수정에 실패하였습니다.", ErrorStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED("FILE-003", "파일 삭제에 실패하였습니다.", ErrorStatus.INTERNAL_SERVER_ERROR),
    INVALID_IMAGE_TYPE("FILE-004", "이미지 타입의 파일만 업로드 가능합니다.", ErrorStatus.BAD_REQUEST),
    NOT_FOUND_PLACES("PLACE-001", "존재하지 않는 공연장입니다.", ErrorStatus.NOT_FOUND),
    NOT_FOUND_GRADE("GRADE-001", "공연장에 매핑된 좌석 등급이 없습니다.", ErrorStatus.NOT_FOUND),
    SEAT_CREATE_FAILED("SEAT-002", "좌석 등록에 실패하였습니다.", ErrorStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND_SEAT_STATUS("SEAT-003", "존재하지 않는 좌석 상태입니다.", ErrorStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("EMAIL-001", "이미 사용하는 이메일입니다.", ErrorStatus.ALREADY_EXISTS),
    FAIL_EMAIL_SEND("EMAIL-002", "메일 전송에 실패하였습니다.", ErrorStatus.BAD_REQUEST),
    NOT_FOUND_AUTH("AUTH-002", "잘못된 인증번호 입니다.", ErrorStatus.NOT_FOUND),
    NOT_FOUND_PROVIDER("PROVIDER-001", "지원하지 않는 플랫폼 서비스입니다.", ErrorStatus.NOT_FOUND),
    NOT_FOUND_BOOK("BOOK-001", "예약 정보가 존재하지 않습니다.", ErrorStatus.NOT_FOUND),
    NOT_FOUND_SCHEDULE("SCHEDULE-001", "존재하지 않는 스케줄 업무입니다.", ErrorStatus.NOT_FOUND),
    UNAUTHORIZED_REQUEST("AUTH-003", "올바른 요청이 아닙니다. 로그인을 해주세요.", ErrorStatus.UNAUTHORIZED),
    INVALID_DATETIME_PERIOD(
        "DATETIME-001",
        "공연 종료 시간은 공연 시작 시간 이후여야 합니다.",
        ErrorStatus.BAD_REQUEST
    ),
    INVALID_DATETIME_TYPE("DATETIME-002", "날짜 형식이 올바르지 않습니다.", ErrorStatus.BAD_REQUEST),
    EMAIL_ALREADY_REGISTERED_WITH_DIFFERENT_PROVIDER(
        "AUTH-004",
        "이미 가입된 이메일입니다. 일반 로그인 방식으로 로그인 해주세요.",
        ErrorStatus.CONFLICT
    );
}
