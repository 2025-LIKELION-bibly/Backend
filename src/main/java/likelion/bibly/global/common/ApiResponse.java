package likelion.bibly.global.common;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	private boolean success;
	private T data;
	private String message;
	private LocalDateTime timestamp;

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null, LocalDateTime.now());
	}

	public static <T> ApiResponse<T> success(T data, String message) {
		return new ApiResponse<>(true, data, message, LocalDateTime.now());
	}

	public static <T> ApiResponse<T> fail(String message) {
		return new ApiResponse<>(false, null, message, LocalDateTime.now());
	}
}
