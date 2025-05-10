package com.employees.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException{

    private ErrorCode errorCode;

    public BadRequestException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}
