package likelion.bibly.global.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	// Common
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 오류가 발생했습니다."),
	ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "엔티티를 찾을 수 없습니다."),
	INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "잘못된 타입입니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근 권한이 없습니다."),

	// User
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
	USER_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "U002", "이미 탈퇴한 사용자입니다."),
	DUPLICATE_USER(HttpStatus.CONFLICT, "U003", "이미 존재하는 사용자입니다."),

	// Group
	GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "G001", "모임을 찾을 수 없습니다."),
	INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "G002", "해당하는 코드의 모임이 없습니다. 다시 입력해 주세요."),
	INVITE_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "G003", "만료된 초대 코드입니다."),
	GROUP_FULL(HttpStatus.BAD_REQUEST, "G004", "해당 모임은 모임원이 가득 차서 가입할 수 없어요."),
	GROUP_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "G005", "모임 이름은 15자까지만 입력할 수 있어요."),
	INVALID_READING_PERIOD(HttpStatus.BAD_REQUEST, "G006", "독서기간은 7일에서 60일 사이여야 합니다."),

	// Member
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "모임원을 찾을 수 없습니다."),
	DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "M002", "이미 사용 중인 닉네임입니다."),
	DUPLICATE_COLOR(HttpStatus.CONFLICT, "M003", "이미 다른 모임원이 선택한 색상입니다."),
	INVALID_NICKNAME_LENGTH(HttpStatus.BAD_REQUEST, "M004", "닉네임은 8자까지만 입력할 수 있어요."),
	INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "M005", "특수문자는 사용할 수 없어요."),
	NOT_GROUP_OWNER(HttpStatus.FORBIDDEN, "M006", "모임장만 실행할 수 있습니다."),
	MEMBER_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "M007", "이미 탈퇴한 모임원입니다."),

	// Book
	BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "책을 찾을 수 없습니다."),
	BOOK_ALREADY_SELECTED(HttpStatus.CONFLICT, "B002", "이미 모임원이 해당 책을 골랐습니다."),

	// Assignment
	ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "배정을 찾을 수 없습니다."),
	REVIEW_TOO_LONG(HttpStatus.BAD_REQUEST, "A002", "40자까지만 입력할 수 있습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
