package com.innowise.paymentservice.kafka.consumer;

import com.innowise.paymentservice.kafka.event.CreatePaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventDlqConsumer {

  private final KafkaTemplate<String, CreatePaymentEvent> kafkaTemplate;

  @Value("${spring.kafka.topics.payment-events}")
  private String topicName;

  @Value("${spring.kafka.topics.payment-events-dlq}")
  private String dlqTopicName;


  @KafkaListener(
      topics = "${spring.kafka.topics.payment-events-dlq}",
      groupId = "${spring.kafka.consumer.group-id}")
  public void consumeDlq(CreatePaymentEvent event) {
    log.info("Consumed event from DLQ: {}", event.getOrderId());
    try {
      kafkaTemplate.send(topicName, event.getOrderId().toString(), event)
          .whenComplete((result, ex) -> {
            if (ex != null) {
              log.error("Failed to resend event from DLQ to main topic. orderId={}", event.getOrderId(), ex);
            } else {
              log.info("Successfully resent event from DLQ to main topic. orderId={}", event.getOrderId());
            }
          });
    } catch (Exception e) {
      log.error("Unexpected error when processing DLQ event. orderId={}", event.getOrderId(), e);
    }
  }
}