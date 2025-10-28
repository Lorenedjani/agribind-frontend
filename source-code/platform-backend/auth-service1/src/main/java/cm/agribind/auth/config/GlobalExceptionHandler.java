package cm.agribind.auth.controller;

import cm.agribind.auth.dto.ApiError;
import cm.agribind.auth.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ApiError> handleBusiness(RuntimeException ex, HttpServletRequest req) {
        HttpStatus status = ex instanceof UserNotFoundException ? HttpStatus.NOT_FOUND : HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status)
                .body(ApiError.builder()
                        .timestamp(Instant.now())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(ex.getMessage())
                        .path(req.getRequestURI())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldError() != null ?
                ex.getBindingResult().getFieldError().getDefaultMessage() : "Validation error";
        return ResponseEntity.badRequest()
                .body(ApiError.builder()
                        .timestamp(Instant.now())
                        .status(400)
                        .error("Bad Request")
                        .message(msg)
                        .path(req.getRequestURI())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(500)
                .body(ApiError.builder()
                        .timestamp(Instant.now())
                        .status(500)
                        .error("Internal Server Error")
                        .message(ex.getMessage())
                        .path(req.getRequestURI())
                        .build());
    }
}
