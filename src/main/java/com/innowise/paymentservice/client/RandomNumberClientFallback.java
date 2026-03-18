package com.innowise.paymentservice.client;

import com.innowise.paymentservice.exception.RandomNumberClientException;
import org.springframework.stereotype.Component;

@Component
public class RandomNumberClientFallback implements RandomNumberClient{

  @Override
  public int[] getRandomNumber(int min, int max, int count) {
    throw  new RandomNumberClientException("Random number service is unavailable");
  }
}
