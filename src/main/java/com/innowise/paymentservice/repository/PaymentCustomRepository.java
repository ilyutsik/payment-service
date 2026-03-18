package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PaymentCustomRepository {

  List<Payment> findByUserIdOrOrderIdOrStatus(Long userId, Long orderId, PaymentStatus status);

  BigDecimal getTotalPaymentsForUser(Long userId, Instant from, Instant to);

  BigDecimal getTotalPaymentsForAll(Instant from, Instant to);
}
