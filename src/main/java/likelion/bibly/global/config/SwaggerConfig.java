package likelion.bibly.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Bibly API")
				.version("v1.0.0")
				.description("Bibly의 백엔드 API 문서입니다."))
			.servers(List.of(
				new Server().url("http://localhost:8080").description("Local Server"),
				new Server().url("https://api.bibly.com").description("Production Server")
			));
	}
}
