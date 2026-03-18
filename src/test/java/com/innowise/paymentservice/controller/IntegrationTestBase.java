package com.innowise.paymentservice.controller;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    LiquibaseAutoConfiguration.class})
public class IntegrationTestBase {

  @Container
  static MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0");

  @Container
  static KafkaContainer kafka = new KafkaContainer(
      DockerImageName.parse("apache/kafka:3.7.0")
  );
  @Container
  static GenericContainer<?> wiremock = new GenericContainer<>(
      "wiremock/wiremock:3.5.4").withExposedPorts(8080);

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    registry.add("random-number-api.url", IntegrationTestBase::wiremockBaseUrl);
  }

  protected static String wiremockBaseUrl() {
    return "http://" + wiremock.getHost() + ":" + wiremock.getMappedPort(8080);
  }
}
