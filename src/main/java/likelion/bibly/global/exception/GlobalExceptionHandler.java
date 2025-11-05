package likelion.bibly.global.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
		log.error("BusinessException: {}", e.getMessage());
		ErrorResponse response = ErrorResponse.of(e.getErrorCode());
		return ResponseEntity
			.status(e.getErrorCode().getStatus())
			.body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationException(
		MethodArgumentNotValidException e) {
		log.error("ValidationException: {}", e.getMessage());

		Map<String, String> errors = new HashMap<>();
		for (FieldError error : e.getBindingResult().getFieldErrors()) {
			errors.put(error.getField(), error.getDefaultMessage());
		}

		Map<String, Object> response = new HashMap<>();
		response.put("code", "C001");
		response.put("message", "입력값 검증에 실패했습니다.");
		response.put("status", 400);
		response.put("errors", errors);
		response.put("timestamp", LocalDateTime.now());

		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
		ErrorResponse response = ErrorResponse.of(ErrorCode.ENTITY_NOT_FOUND);
		return ResponseEntity.status(404).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		log.error("Exception: ", e);
		ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
		return ResponseEntity.internalServerError().body(response);
	}
}
