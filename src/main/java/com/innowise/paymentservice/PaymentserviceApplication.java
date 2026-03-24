package com.innowise.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableFeignClients
@EnableMethodSecurity
@SpringBootApplication
public class PaymentserviceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentserviceApplication.class, args);
  }
}