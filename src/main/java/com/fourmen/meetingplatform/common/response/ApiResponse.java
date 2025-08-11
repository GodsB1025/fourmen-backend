package com.fourmen.meetingplatform.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final String result;
    private final String message;
    private final T data;

    private ApiResponse(String result, String message, T data) {
        this.result = result;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>("SUCCESS", message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }
}