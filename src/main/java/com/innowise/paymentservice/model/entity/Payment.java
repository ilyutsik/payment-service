package com.innowise.paymentservice.model.entity;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

  @Id
  private String id;

  @Field("user_id")
  private Long userId;

  @Field("order_id")
  private Long orderId;

  @Field("status")
  private PaymentStatus status;

  @Field("timestamp")
  private Instant timestamp;

  @Field("payment_amount")
  private BigDecimal paymentAmount;
}
