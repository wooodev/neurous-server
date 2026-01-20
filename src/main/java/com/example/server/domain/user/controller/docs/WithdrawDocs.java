package com.example.server.domain.user.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "회원 탈퇴",
        description = """
                회원 탈퇴를 진행합니다.

                - unlinkSocial=true: 소셜 연결 끊기 시도
                - providerAccessToken: 구글/네이버/카카오 등의 provider accessToekn(유저 accessToken과 다름)
                - appleAuthorizationCode: Apple 연결 끊기에 필요한 값(Apple 로그인 유저일 때)
                """
)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
        @ApiResponse(
                responseCode = "400",
                description = "요청값 검증 실패",
                content = @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        examples = @ExampleObject(
                                name = "BadRequest",
                                value = """
                                {
                                  "success": false,
                                  "code": "USER4000",
                                  "message": "요청값이 올바르지 않습니다."
                                }
                                """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "인증 실패(Authorization Bearer 필요)",
                content = @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        examples = @ExampleObject(
                                name = "Unauthorized",
                                value = """
                                {
                                  "success": false,
                                  "code": "AUTH1008",
                                  "message": "인증이 필요합니다."
                                }
                                """
                        )
                )
        ),
        @ApiResponse(responseCode = "500", description = "서버 오류")
})
public @interface WithdrawDocs {
}
