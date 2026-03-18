package com.innowise.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "randomize", url = "${random-number-api.url}")
public interface RandomNumberClient {

  @GetMapping("api/v1.0/random")
  int[] getRandomNumber(@RequestParam int min, @RequestParam int max, @RequestParam int count);

  default int getRandomNumber() {
    return getRandomNumber(1, 100, 1)[0];
  }
}
