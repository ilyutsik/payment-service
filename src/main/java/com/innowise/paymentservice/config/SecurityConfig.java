package com.innowise.paymentservice.config;

import com.innowise.paymentservice.config.security.GatewayAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final GatewayAuthenticationFilter gatewayFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .httpBasic(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .addFilterBefore(gatewayFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/swagger-ui/**", "/v3/api-docs/**"
            ).permitAll()
            .anyRequest().authenticated()
        ).exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint((request, response, authException) -> {
              response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
              response.setContentType(MediaType.APPLICATION_JSON_VALUE);
              response.getWriter().write("{\"error\": \"Authentication required\"}");
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.setStatus(HttpServletResponse.SC_FORBIDDEN);
              response.setContentType(MediaType.APPLICATION_JSON_VALUE);
              response.getWriter().write("{\"error\": \"Access denied\"}");
            })
        )
        .build();
  }
}