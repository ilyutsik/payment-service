package com.innowise.paymentservice.exception;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex) {
    ErrorResponse errorResponse = buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(RandomNumberClientException.class)
  public ResponseEntity<ErrorResponse> handlePaymentNotFound(RandomNumberClientException ex) {
    ErrorResponse errorResponse = buildErrorResponse(ex, HttpStatus.SERVICE_UNAVAILABLE);
    return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {

    String error = ex.getBindingResult()
        .getFieldErrors()
        .getFirst()
        .getDefaultMessage();

    ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), error,
        HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  private ErrorResponse buildErrorResponse(Exception ex, HttpStatus status) {
    return new ErrorResponse(status.getReasonPhrase(), ex.getMessage(), status.value(),
        LocalDateTime.now());
  }
}
