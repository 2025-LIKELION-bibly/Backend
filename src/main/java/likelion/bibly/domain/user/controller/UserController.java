package likelion.bibly.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.bibly.domain.user.dto.response.UserCreateResponse;
import likelion.bibly.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@Operation(
		summary = "사용자 생성",
		description = """
            새로운 UUID 기반 사용자를 생성합니다. 로컬스토리지 사용하시면 됩니다.
            
            **로컬스토리지:**
            1. API 호출
            2. 반환된 userId를 localStorage에 저장
            3. 이후 모든 API 요청 시 헤더에 "X-User-Id: {userId}" 포함
            """
	)
	@PostMapping
	public ResponseEntity<UserCreateResponse> createUser() {
		UserCreateResponse response = userService.createUser();
		return ResponseEntity.ok(response);
	}
}
