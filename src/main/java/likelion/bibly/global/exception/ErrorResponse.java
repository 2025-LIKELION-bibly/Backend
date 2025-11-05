package likelion.bibly.global.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
	private String code;
	private String message;
	private int status;
	private LocalDateTime timestamp;

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(
			errorCode.getCode(),
			errorCode.getMessage(),
			errorCode.getStatus().value(),
			LocalDateTime.now()
		);
	}
}
