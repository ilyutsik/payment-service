package com.innowise.paymentservice.exception;

import java.io.Serial;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PaymentNotFoundException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -2242698683751732704L;

  public PaymentNotFoundException(String field, String value) {
    super("Payment with " + field + " : " + value + " not found");
  }

  public PaymentNotFoundException(String message) {
    super(message);
  }

  public PaymentNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
