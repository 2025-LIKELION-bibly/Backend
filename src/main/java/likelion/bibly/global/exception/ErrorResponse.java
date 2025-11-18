package likelion.bibly.global.exception;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 에러 응답 DTO
 */
@Schema(description = "에러 응답")
@Getter
@AllArgsConstructor
public class ErrorResponse {
	@Schema(description = "에러 코드")
	private String code;

	@Schema(description = "에러 메시지")
	private String message;

	@Schema(description = "HTTP 상태 코드")
	private int status;

	@Schema(description = "에러 발생 시간")
	private LocalDateTime timestamp;

	/**
	 * ErrorCode로부터 ErrorResponse 생성
	 *
	 * @param errorCode 에러 코드 enum
	 * @return ErrorResponse 객체
	 */
	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(
			errorCode.getCode(),
			errorCode.getMessage(),
			errorCode.getStatus().value(),
			LocalDateTime.now()
		);
	}
}
