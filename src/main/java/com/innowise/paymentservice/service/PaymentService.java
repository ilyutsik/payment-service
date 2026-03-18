package com.innowise.paymentservice.service;

import com.innowise.paymentservice.model.dto.RequestPaymentDto;
import com.innowise.paymentservice.model.dto.ResponsePaymentDto;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PaymentService {

  ResponsePaymentDto create(RequestPaymentDto dto);

  List<ResponsePaymentDto> getByUserIdOrOrderIdOrStatus(Long userId, Long orderId,
      PaymentStatus status);

  BigDecimal getUserTotalSumOfPayments(Long userId, Instant from, Instant to);

  BigDecimal geAllUsersTotalSumPayments(Instant from, Instant to);
}
