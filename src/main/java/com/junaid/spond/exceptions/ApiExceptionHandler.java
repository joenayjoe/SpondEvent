package com.junaid.spond.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ApiExceptionHandler {
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex, HttpServletRequest request) {
    var error =
        ErrorResponse.builder()
            .title("Resource Not Found")
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .occurredAt(LocalDateTime.now())
            .httpMethod(request.getMethod())
            .path(request.getRequestURI())
            .build();
    log.error("Resource Not Found Exception", ex);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    var error =
        ErrorResponse.builder()
            .title("Validation Failed")
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Input validation failed")
            .occurredAt(LocalDateTime.now())
            .httpMethod(request.getMethod())
            .path(request.getRequestURI())
            .build();

    List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

    for (FieldError fe : fieldErrors) {
      List<ValidationError> validationErrorList = error.getErrors().get(fe.getField());
      if (validationErrorList == null) {
        validationErrorList = new ArrayList<>();
        error.getErrors().put(fe.getField(), validationErrorList);
      }
      ValidationError validationError = new ValidationError();
      validationError.setCode(fe.getCode());
      validationError.setMessage(fe.getDefaultMessage());

      validationErrorList.add(validationError);
    }
    log.error("Validation Exception", ex);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(
      Exception ex, HttpServletRequest request) {
    var error =
        ErrorResponse.builder()
            .title("Internal Server Error")
            .status(HttpStatus.INSUFFICIENT_STORAGE.value())
            .message(ex.getMessage())
            .occurredAt(LocalDateTime.now())
            .httpMethod(request.getMethod())
            .path(request.getRequestURI())
            .build();
    log.error("Internal Server Error", ex);
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
