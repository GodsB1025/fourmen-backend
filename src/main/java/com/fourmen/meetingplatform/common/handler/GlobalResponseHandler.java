package com.fourmen.meetingplatform.common.handler;

import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.common.response.ApiResponse;
import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.common.response.BypassApiResponse;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.fourmen.meetingplatform")
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.hasMethodAnnotation(BypassApiResponse.class);
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {

        if (body instanceof ApiResponse) {
            return body;
        }

        ApiResponseMessage messageAnnotation = returnType.getMethodAnnotation(ApiResponseMessage.class);
        String message = (messageAnnotation != null) ? messageAnnotation.value() : "성공";

        if (body instanceof String) {
            return ApiResponse.success(body, message);
        }

        return ApiResponse.success(body, message);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
    }
}