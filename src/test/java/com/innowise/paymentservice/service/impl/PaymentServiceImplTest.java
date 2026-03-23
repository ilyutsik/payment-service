package com.innowise.paymentservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.paymentservice.client.RandomNumberClient;
import com.innowise.paymentservice.kafka.event.CreatePaymentEvent;
import com.innowise.paymentservice.kafka.producer.PaymentEventProducer;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.dto.RequestPaymentDto;
import com.innowise.paymentservice.model.dto.ResponsePaymentDto;
import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import com.innowise.paymentservice.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PaymentServiceImplTest {

  @Mock
  private PaymentEventProducer paymentEventProducer;

  @Mock
  private RandomNumberClient randomNumberClient;

  @Mock
  private PaymentRepository repository;

  @Mock
  private PaymentMapper mapper;

  @InjectMocks
  private PaymentServiceImpl paymentService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreatePayment_success() {
    RequestPaymentDto request = new RequestPaymentDto();
    Payment payment = new Payment();
    payment.setOrderId(1L);

    ResponsePaymentDto responseDto = new ResponsePaymentDto();

    when(mapper.toEntity(request)).thenReturn(payment);
    when(randomNumberClient.getRandomNumber()).thenReturn(2);
    when(repository.save(payment)).thenReturn(payment);
    when(mapper.toDto(payment)).thenReturn(responseDto);

    ResponsePaymentDto result = paymentService.create(request);

    assertNotNull(result);
    assertEquals(responseDto, result);
    assertEquals(PaymentStatus.SUCCESS, payment.getStatus());

    verify(paymentEventProducer).sendPaymentEvent(any(CreatePaymentEvent.class));
    verify(repository).save(payment);
  }

  @Test
  void testCreatePayment_failed() {
    RequestPaymentDto request = new RequestPaymentDto();
    Payment payment = new Payment();

    ResponsePaymentDto responseDto = new ResponsePaymentDto();

    when(mapper.toEntity(request)).thenReturn(payment);
    when(randomNumberClient.getRandomNumber()).thenReturn(3);
    when(repository.save(payment)).thenReturn(payment);
    when(mapper.toDto(payment)).thenReturn(responseDto);

    ResponsePaymentDto result = paymentService.create(request);

    assertNotNull(result);
    assertEquals(PaymentStatus.FAILED, payment.getStatus());
    verify(paymentEventProducer).sendPaymentEvent(any(CreatePaymentEvent.class));
    verify(repository).save(payment);
  }

  @Test
  void testGetByUserIdOrOrderIdOrStatus() {
    Payment payment = new Payment();
    ResponsePaymentDto dto = new ResponsePaymentDto();

    when(repository.findByUserIdOrOrderIdOrStatus(1L, 2L, PaymentStatus.SUCCESS)).thenReturn(
        List.of(payment));
    when(mapper.toDto(payment)).thenReturn(dto);

    List<ResponsePaymentDto> result = paymentService.getByUserIdOrOrderIdOrStatus(1L, 2L,
        PaymentStatus.SUCCESS);

    assertEquals(1, result.size());
    assertEquals(dto, result.get(0));
    verify(repository).findByUserIdOrOrderIdOrStatus(1L, 2L, PaymentStatus.SUCCESS);
  }

  @Test
  void testGetUserTotalSumOfPayments() {
    Instant from = Instant.parse("2025-03-17T15:00:00Z");
    Instant to = Instant.parse("2027-03-17T15:00:00Z");

    when(repository.getTotalPaymentsForUser(1L, from, to)).thenReturn(
        BigDecimal.valueOf(100));

    BigDecimal total = paymentService.getUserTotalSumOfPayments(1L, from, to);

    assertEquals(BigDecimal.valueOf(100), total);
    verify(repository).getTotalPaymentsForUser(1L, from, to);
  }

  @Test
  void testGetAllUsersTotalSumPayments() {
    Instant from = Instant.parse("2025-03-17T15:00:00Z");
    Instant to = Instant.parse("2027-03-17T15:00:00Z");

    when(repository.getTotalPaymentsForAll(from, to)).thenReturn(
        BigDecimal.valueOf(500));

    BigDecimal total = paymentService.getAllUsersTotalSumPayments(from, to);

    assertEquals(BigDecimal.valueOf(500), total);
    verify(repository).getTotalPaymentsForAll(from, to);
  }
}