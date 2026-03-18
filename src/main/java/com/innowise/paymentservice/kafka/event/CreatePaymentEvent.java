package com.innowise.paymentservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentEvent {

  private Long orderId;
  private String paymentStatus;
}