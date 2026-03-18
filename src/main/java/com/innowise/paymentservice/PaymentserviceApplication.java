package com.innowise.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableFeignClients
@EnableMongoRepositories
@SpringBootApplication
public class PaymentserviceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentserviceApplication.class, args);
  }
}