package com.innowise.paymentservice.kafka.producer;

import com.innowise.paymentservice.kafka.event.CreatePaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

  private final KafkaTemplate<String, CreatePaymentEvent> kafkaTemplate;

  @Value("${spring.kafka.topics.payment-events}")
  private String topicName;

  @Value("${spring.kafka.topics.payment-events-dlq}")
  private String dlqTopicName;

  public void sendPaymentEvent(CreatePaymentEvent paymentEvent) {
    String orderId = paymentEvent.getOrderId().toString();

    kafkaTemplate.send(topicName, orderId, paymentEvent)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            log.error("Failed to publish PaymentEvent to main topic. orderId={}. Sending to DLQ...", orderId, ex);
            sendToDLQ(paymentEvent);
          } else {
            log.info(
                "PaymentEvent successfully published. orderId={}, topic={}, partition={}, offset={}",
                orderId, topicName, result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
          }
        });
  }

  private void sendToDLQ(CreatePaymentEvent event) {
    try {
      kafkaTemplate.send(dlqTopicName, event.getOrderId().toString(), event)
          .whenComplete((res, ex) -> {
            if (ex != null) {
              log.error("Failed to publish PaymentEvent to DLQ as well! orderId={}", event.getOrderId(), ex);
            } else {
              log.info("PaymentEvent successfully sent to DLQ. orderId={}, dlqTopic={}", event.getOrderId(), dlqTopicName);
            }
          });
    } catch (Exception e) {
      log.error("Unexpected error when sending event to DLQ. orderId={}", event.getOrderId(), e);
    }
  }
}