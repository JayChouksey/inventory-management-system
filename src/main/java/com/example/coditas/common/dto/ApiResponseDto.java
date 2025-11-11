package com.example.coditas.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

    private boolean success;
    private String message;
    private T data;
    private Map<String, Object> meta;


    public static <T> ApiResponseDto<T> ok(T data) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponseDto<T> ok(T data, String message) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponseDto<T> paged(T data, int page, int size, long total) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .meta(Map.of(
                        "page", page,
                        "size", size,
                        "totalElements", total,
                        "totalPages", (int) Math.ceil((double) total / size)
                ))
                .build();
    }

    public static <T> ApiResponseDto<T> error(String message, HttpStatus status) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

}
