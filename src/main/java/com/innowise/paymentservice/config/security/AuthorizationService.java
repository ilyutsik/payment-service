package com.innowise.paymentservice.config.security;

import com.innowise.paymentservice.model.dto.RequestPaymentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

  public boolean isOwner(Long userId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth.getName().equals(userId.toString());
  }

  public boolean isOwner(RequestPaymentDto requestPaymentDto) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth.getName().equals(requestPaymentDto.getUserId().toString());
  }
}
