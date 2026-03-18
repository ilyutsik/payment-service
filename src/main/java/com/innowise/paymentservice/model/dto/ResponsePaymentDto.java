package com.innowise.paymentservice.model.dto;

import com.innowise.paymentservice.model.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponsePaymentDto extends BasePaymentDto {
  
  private PaymentStatus status;
}
