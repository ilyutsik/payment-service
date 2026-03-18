package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.config.security.annotation.AdminOnly;
import com.innowise.paymentservice.config.security.annotation.OwnerOnly;
import com.innowise.paymentservice.config.security.annotation.OwnerOrAdmin;
import com.innowise.paymentservice.model.dto.RequestPaymentDto;
import com.innowise.paymentservice.model.dto.ResponsePaymentDto;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import com.innowise.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  @OwnerOnly
  @PostMapping
  public ResponseEntity<ResponsePaymentDto> create(
      @Valid @RequestBody RequestPaymentDto requestPaymentDto) {
    ResponsePaymentDto response = paymentService.create(requestPaymentDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @AdminOnly
  @GetMapping
  public ResponseEntity<List<ResponsePaymentDto>> getByParams(
      @RequestParam(required = false) Long userId, @RequestParam(required = false) Long orderId,
      @RequestParam(required = false) PaymentStatus status) {
    List<ResponsePaymentDto> response = paymentService.getByUserIdOrOrderIdOrStatus(userId, orderId,
        status);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @OwnerOrAdmin
  @GetMapping("/summary/{userId}")
  public ResponseEntity<BigDecimal> getUserPaymentsSumForAdmin(
      @PathVariable Long userId, @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to) {
    BigDecimal response = paymentService.getUserTotalSumOfPayments(userId, from, to);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @AdminOnly
  @GetMapping("/summary")
  public ResponseEntity<BigDecimal> getAllUsersPaymentsSum(
      @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to) {
    BigDecimal response = paymentService.geAllUsersTotalSumPayments(from, to);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
