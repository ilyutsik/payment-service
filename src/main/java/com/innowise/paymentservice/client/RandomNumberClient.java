package com.innowise.paymentservice.client;

import com.innowise.paymentservice.exception.RandomNumberClientException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "randomize", url = "${random-number-api.url}", fallback = RandomNumberClientFallback.class)
public interface RandomNumberClient {

  @GetMapping("api/v1.0/random")
  int[] getRandomNumber(@RequestParam int min, @RequestParam int max, @RequestParam int count);

  default int getRandomNumber() {
    int[] result = getRandomNumber(1, 100, 1);

    if (result == null || result.length == 0) {
      throw new RandomNumberClientException("Random number API returned empty response");
    }

    return result[0];
  }
}
