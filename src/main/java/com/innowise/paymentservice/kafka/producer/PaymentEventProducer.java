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

  public void sendPaymentEvent(CreatePaymentEvent paymentEvent) {
    String orderId = paymentEvent.getOrderId().toString();
    kafkaTemplate.send(topicName, orderId, paymentEvent)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            log.error("Failed to publish PaymentEvent. orderId={}, topic={}",
                orderId, topicName, ex);
          } else {
            log.info(
                "PaymentEvent successfully published. orderId={}, topic={}, partition={}, offset={}",
                orderId, topicName, result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
          }
        });
  }
}