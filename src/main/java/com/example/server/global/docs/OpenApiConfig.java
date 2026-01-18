package com.example.server.global.docs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

	@Value("${app.backend.base-url}")
	private String backendBaseUrl;

	@Bean
	public OpenAPI customOpenApi() {
		final String securitySchemeName = "Bearer Token";

		return new OpenAPI()
			.addServersItem(new Server().url(backendBaseUrl))
			.info(new Info()
				.title("API 문서")
				.version("v1.0")
				.description("Neurous 프로젝트 Swagger 문서입니다.\n\n"
					+ "토큰 인증이 필요한 API는 상단의 Authorize 버튼을 클릭한 뒤, `토큰`만 입력해주세요.\n"
					+ "\nBearer는 자동으로 붙으므로, eyJhbGciOi... 와 같은 순수 토큰 문자열만 입력하시면 됩니다.")
			)
			.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
			.components(new Components()
				.addSecuritySchemes(securitySchemeName,
					new SecurityScheme()
						.name(securitySchemeName)
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.in(SecurityScheme.In.HEADER)
				)
				.addSchemas(
					"ErrorResponse",
					new Schema<>()
						.description("공통 에러 응답")
						.addProperty("status",
							new IntegerSchema()
								.example(400)
								.description("HTTP 상태 코드"))
						.addProperty("code",
							new StringSchema()
								.example("INVALID_REQUEST")
								.description("에러 코드"))
						.addProperty("message",
							new StringSchema()
								.example("유효하지 않은 요청입니다."))
				)
			);
	}
}
