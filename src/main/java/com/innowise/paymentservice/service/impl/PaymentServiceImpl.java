package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.client.RandomNumberClient;
import com.innowise.paymentservice.kafka.event.CreatePaymentEvent;
import com.innowise.paymentservice.kafka.producer.PaymentEventProducer;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.dto.RequestPaymentDto;
import com.innowise.paymentservice.model.dto.ResponsePaymentDto;
import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentEventProducer paymentEventProducer;
  private final RandomNumberClient randomNumberClient;
  private final PaymentRepository repository;
  private final PaymentMapper mapper;

  @Override
  public ResponsePaymentDto create(RequestPaymentDto requestPaymentDto) {
    Payment newPayment = toEntity(requestPaymentDto);
    if (randomNumberClient.getRandomNumber() % 2 == 0) {
      newPayment.setStatus(PaymentStatus.SUCCESS);
    } else {
      newPayment.setStatus(PaymentStatus.FAILED);
    }
    Payment savedPayment = repository.save(newPayment);

    CreatePaymentEvent createPaymentEvent = new CreatePaymentEvent(savedPayment.getOrderId(),
        savedPayment.getStatus().name());

    paymentEventProducer.sendPaymentEvent(createPaymentEvent);
    return toDto(savedPayment);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ResponsePaymentDto> getByUserIdOrOrderIdOrStatus(Long userId, Long orderId,
      PaymentStatus status) {
    List<Payment> payments = repository.findByUserIdOrOrderIdOrStatus(userId, orderId, status);
    return payments.stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal getUserTotalSumOfPayments(Long userId, Instant from,
      Instant to) {
    return repository.getTotalPaymentsForUser(userId, from, to);
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal geAllUsersTotalSumPayments(Instant from, Instant to) {
    return repository.getTotalPaymentsForAll(from, to);
  }

  private Payment toEntity(RequestPaymentDto dto) {
    return mapper.toEntity(dto);
  }

  private ResponsePaymentDto toDto(Payment payment) {
    return mapper.toDto(payment);
  }
}
