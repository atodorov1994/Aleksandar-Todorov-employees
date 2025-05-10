package com.employees.exception;

import com.employees.model.ErrorResponse;
import com.employees.model.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    protected ResponseEntity<Response<ErrorResponse>> handleRuntime(RuntimeException ex) {

        return ResponseEntity
                .internalServerError()
                .body(new Response<>(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), new ErrorResponse("Internal server error")));
    }

    @ExceptionHandler(value = BadRequestException.class)
    protected ResponseEntity<Response<ErrorResponse>> handleBadRequest(BadRequestException ex) {

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(new Response<>(ex.getErrorCode().getCode(), new ErrorResponse(ex.getMessage())));
    }

    @ExceptionHandler(value = NotFoundException.class)
    protected ResponseEntity<Response<ErrorResponse>> handleNotFound(NotFoundException ex) {

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(new Response<>(ex.getErrorCode().getCode(), new ErrorResponse(ex.getMessage())));
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new Response<>(ErrorCode.INVALID_PARAM.getCode(), new ErrorResponse(ex.getMessage())));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new Response<>(ErrorCode.INVALID_PARAM.getCode(), new ErrorResponse(ex.getMessage())));
    }
}
