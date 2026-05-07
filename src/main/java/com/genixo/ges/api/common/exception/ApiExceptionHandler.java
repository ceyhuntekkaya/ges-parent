package com.genixo.ges.api.common.exception;

import com.genixo.ges.api.common.dto.ApiErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiErrorDto.FieldViolationDto> fields = ex.getBindingResult()
            .getAllErrors()
            .stream()
            .map(err -> {
                if (err instanceof FieldError fe) {
                    return ApiErrorDto.FieldViolationDto.builder()
                        .field(fe.getField())
                        .message(fe.getDefaultMessage())
                        .build();
                }
                return ApiErrorDto.FieldViolationDto.builder()
                    .field(err.getObjectName())
                    .message(err.getDefaultMessage())
                    .build();
            })
            .collect(Collectors.toList());

        ApiErrorDto body = base(req, HttpStatus.BAD_REQUEST)
            .message("Validation failed")
            .fieldViolations(fields)
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDto> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        ApiErrorDto body = base(req, HttpStatus.FORBIDDEN)
            .message("Forbidden")
            .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorDto> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        ApiErrorDto body = base(req, HttpStatus.UNAUTHORIZED)
            .message("Invalid credentials")
            .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(ApiProblemException.class)
    public ResponseEntity<ApiErrorDto> handleApiProblem(ApiProblemException ex, HttpServletRequest req) {
        ApiErrorDto body = base(req, ex.getStatus())
            .message(ex.getMessage())
            .build();
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleGeneric(Exception ex, HttpServletRequest req) {
        String traceId = UUID.randomUUID().toString();
        log.error("Unhandled exception (traceId={}, path={})", traceId, req.getRequestURI(), ex);
        ApiErrorDto body = base(req, HttpStatus.INTERNAL_SERVER_ERROR, traceId)
            .message("Unexpected error")
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ApiErrorDto.ApiErrorDtoBuilder base(HttpServletRequest req, HttpStatus status) {
        return base(req, status, UUID.randomUUID().toString());
    }

    private ApiErrorDto.ApiErrorDtoBuilder base(HttpServletRequest req, HttpStatus status, String traceId) {
        return ApiErrorDto.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .path(req.getRequestURI())
            .traceId(traceId);
    }
}

