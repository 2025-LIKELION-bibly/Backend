package likelion.bibly.global.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Value("${spring.profiles.active:local}")
	private String activeProfile;

	@Bean
	public OpenAPI openAPI() {
		List<Server> servers = new ArrayList<>();

		// 프로필별 서버 URL 설정
		if ("local".equals(activeProfile)) {
			servers.add(new Server().url("http://localhost:8080").description("Local Development"));
		} else if ("prod".equals(activeProfile)) {
			// 프로덕션: 실제 도메인만 표시 (프론트엔드가 헷갈리지 않게)
			servers.add(new Server().url("http://bib-ly.kro.kr").description("Production Server"));
		} else {
			// 기타 환경: 모든 서버 표시
			servers.add(new Server().url("http://localhost:8080").description("Local"));
			servers.add(new Server().url("http://bib-ly.kro.kr").description("Development"));
		}

		return new OpenAPI()
			.info(new Info()
				.title("Bibly API")
				.version("v1.0.0")
				.description("Bibly의 백엔드 API 문서입니다."))
			.addSecurityItem(new SecurityRequirement().addList("X-User-Id"))
			.components(new Components()
				.addSecuritySchemes("X-User-Id", new SecurityScheme()
					.type(SecurityScheme.Type.APIKEY)
					.in(SecurityScheme.In.HEADER)
					.name("X-User-Id")))
			.servers(servers);
	}
}
