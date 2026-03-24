package com.innowise.paymentservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfiguration {

  @Value("${spring.kafka.topics.payment-events}")
  private String mainTopic;

  @Value("${spring.kafka.topics.payment-events-dlq}")
  private String dlqTopic;

  @Bean
  public NewTopic paymentTopic() {
    return TopicBuilder.name(mainTopic)
          .partitions(4)
          .replicas(1)
          .build();
  }

  @Bean
  public NewTopic dlqPaymentTopic() {
    return TopicBuilder
          .name(dlqTopic)
          .partitions(4)
          .replicas(1)
          .build();
  }
}