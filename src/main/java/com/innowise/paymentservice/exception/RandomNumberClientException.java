package com.innowise.paymentservice.exception;

import java.io.Serial;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RandomNumberClientException extends RuntimeException{

  @Serial
  private static final long serialVersionUID = 7639526200632191729L;

  public RandomNumberClientException(String message) {
    super(message);
  }

  public RandomNumberClientException(String message, Throwable cause) {
    super(message, cause);
  }

  public RandomNumberClientException(Throwable cause) {
    super(cause);
  }
}
