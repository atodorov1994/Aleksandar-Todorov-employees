package com.employees.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(-1, HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_PARAM(1, HttpStatus.BAD_REQUEST),
    INVALID_CSV_FORMAT(2, HttpStatus.BAD_REQUEST),
    NO_EMPLOYEES_WORKED_TOGETHER(3, HttpStatus.BAD_REQUEST),
    ;

    private final int code;
    private final HttpStatus httpStatus;
}
