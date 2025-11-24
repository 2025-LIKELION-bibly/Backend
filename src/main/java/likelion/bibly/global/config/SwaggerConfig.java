package likelion.bibly.global.config;

import java.util.List;

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

	@Bean
	public OpenAPI openAPI() {
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
			.servers(List.of(
				new Server().url("http://localhost:8080").description("Local"),
				new Server().url("http://bib-ly.kro.kr").description("Development"),
				new Server().url("https://api.bibly.com").description("Production")
			));
	}
}
