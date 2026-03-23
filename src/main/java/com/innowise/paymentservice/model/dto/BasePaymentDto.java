package com.innowise.paymentservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class BasePaymentDto {

  @NotNull(message = "User ID must be provided")
  private Long userId;

  @NotNull(message = "Order ID must be provided")
  private Long orderId;

  @NotNull(message = "Timestamp must be provided")
  @Schema(example = "2026-03-14T15:00:00Z")
  private Instant timestamp;

  @NotNull(message = "Payment amount must be provided")
  @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0.1")
  @Schema(example = "100.50")

  private BigDecimal paymentAmount;
}
