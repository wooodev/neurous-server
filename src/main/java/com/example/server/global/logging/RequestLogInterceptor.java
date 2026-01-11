package com.example.server.global.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final String START_AT = "REQ_START_AT";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_AT, System.currentTimeMillis());
        log.info("[REQ-START] {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long start = (long) request.getAttribute(START_AT);
        long tookMs = System.currentTimeMillis() - start;

        if (ex != null) {
            log.error("[REQ-END] {} {} -> {} ({}ms) ex={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), tookMs, ex.toString(), ex);
            return;
        }

        log.info("[REQ-END] {} {} -> {} ({}ms)",
                request.getMethod(), request.getRequestURI(), response.getStatus(), tookMs);
    }
}