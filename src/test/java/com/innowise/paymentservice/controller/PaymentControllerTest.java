package com.innowise.paymentservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.paymentservice.model.dto.RequestPaymentDto;
import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import com.innowise.paymentservice.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
    "spring.kafka.topics.payment-events=create-payment-test"})
class PaymentControllerTest extends IntegrationTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private PaymentRepository paymentRepository;

  @BeforeEach
  void setup() {
    WireMock.configureFor(
        wiremock.getHost(),
        wiremock.getMappedPort(8080)
    );
    WireMock.reset();

    paymentRepository.deleteAll();
  }

  private RequestPostProcessor requestHeaders(Long userId, String role) {
    return request -> {
      request.addHeader("X-USER-ID", userId.toString());
      request.addHeader("X-USER-ROLE", "ROLE_" + role);
      return request;
    };
  }

  private void stubRandomNumberApi(Long number) {
    WireMock.stubFor(WireMock.get(WireMock.urlMatching("/api/v1.0/random.*"))
        .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("[%d]".formatted(number))));
  }

  private RequestPaymentDto createRequestPaymentDto(Long orderId, Long userId) {
    RequestPaymentDto dto = new RequestPaymentDto();
    dto.setUserId(userId);
    dto.setOrderId(orderId);
    dto.setTimestamp(Instant.now());
    dto.setPaymentAmount(new BigDecimal("50.50"));
    return dto;
  }

  @Test
  void createPayment_WhenEvenNumber_ShouldReturnCreatedWithSuccessStatus() throws Exception {
    stubRandomNumberApi(2L);

    mockMvc.perform(post("/api/v1/payments")
            .with(csrf())
            .with(requestHeaders(1L, "ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestPaymentDto(1L, 1L))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.orderId").value(1))
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.status").value("SUCCESS"));

    List<Payment> payments = paymentRepository.findAll();
    assertThat(payments).hasSize(1);
    assertThat(payments.getFirst().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    assertThat(payments.getFirst().getOrderId()).isEqualTo(1L);
  }

  @Test
  void createPayment_WhenOddNumber_ShouldReturnCreatedWithFailedStatus() throws Exception {
    stubRandomNumberApi(1L);

    mockMvc.perform(post("/api/v1/payments")
            .with(requestHeaders(1L, "ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestPaymentDto(1L, 1L))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.orderId").value(1))
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.status").value("FAILED"));

    List<Payment> payments = paymentRepository.findAll();
    assertThat(payments).hasSize(1);
    assertThat(payments.getFirst().getStatus()).isEqualTo(PaymentStatus.FAILED);
    assertThat(payments.getFirst().getUserId()).isEqualTo(1L);
  }

  @Test
  void createPayment_WhenMissingRequestHeaders_ShouldReturn401() throws Exception {
    mockMvc.perform(post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestPaymentDto(1L, 1L))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void createPayment_WhenInvalidDto_ShouldReturn400() throws Exception {
    RequestPaymentDto invalidDto = new RequestPaymentDto();

    mockMvc.perform(post("/api/v1/payments")
            .with(requestHeaders(1L, "USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").isNotEmpty());
  }

  @Test
  void getPayments_WithoutFilters_ShouldReturnAllPayments() throws Exception {
    stubRandomNumberApi(1L);
    mockMvc.perform(post("/api/v1/payments")
            .with(requestHeaders(1L, "USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestPaymentDto(1L, 1L))))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/v1/payments")
            .with(requestHeaders(1L, "ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].userId").value(1))
        .andExpect(jsonPath("$[0].orderId").value(1));
  }

  @Test
  void getPayments_WithUserIdFilter_ShouldReturnOnlyUserPayments() throws Exception {
    stubRandomNumberApi(2L);
    mockMvc.perform(post("/api/v1/payments")
            .with(requestHeaders(1L, "USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestPaymentDto(1L, 1L))))
        .andExpect(status().isCreated());

    stubRandomNumberApi(2L);
    mockMvc.perform(post("/api/v1/payments")
            .with(requestHeaders(2L, "USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestPaymentDto(2L, 2L))))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/v1/payments")
            .with(requestHeaders(1L, "ADMIN"))
            .param("userId", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].userId").value(1))
        .andExpect(jsonPath("$[0].orderId").value(1));
  }

  @Test
  void getPayments_WithStatusFilter_ShouldReturnOnlyMatchingPayments() throws Exception {
    stubRandomNumberApi(2L);
    mockMvc.perform(post("/api/v1/payments")
            .with(requestHeaders(1L, "USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestPaymentDto(1L, 1L))))
        .andExpect(status().isCreated());

    stubRandomNumberApi(3L);
    mockMvc.perform(post("/api/v1/payments")
            .with(requestHeaders(1L, "USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestPaymentDto(2L, 1L))))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/v1/payments")
            .with(requestHeaders(1L, "ADMIN"))
            .param("status", "SUCCESS"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].userId").value(1))
        .andExpect(jsonPath("$[0].orderId").value(1));
  }

  @Test
  void getTotalSumForUser_WhenPaymentsExist_ShouldReturnCorrectSum() throws Exception {
    stubRandomNumberApi(2L);
    mockMvc.perform(post("/api/v1/payments")
            .with(requestHeaders(1L, "USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestPaymentDto(1L, 1L))))
        .andExpect(status().isCreated());

    stubRandomNumberApi(6L);
    mockMvc.perform(post("/api/v1/payments")
            .with(csrf())
            .with(requestHeaders(1L, "USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestPaymentDto(2L, 1L))))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/v1/payments/summary/1")
            .with(requestHeaders(1L, "USER")))
        .andExpect(status().isOk())
        .andExpect(content().string("101.00"));
  }

  @Test
  void getTotalSumForUser_WithoutPayments_ShouldReturnZero() throws Exception {
    mockMvc.perform(get("/api/v1/payments/summary/1")
            .with(requestHeaders(1L, "USER")))
        .andExpect(status().isOk())
        .andExpect(content().string("0"));
  }

  @Test
  void getTotalSumForUser_WhenDateRangeProvided_ShouldReturnCorrectSum() throws Exception {
    stubRandomNumberApi(2L);
    mockMvc.perform(post("/api/v1/payments")
            .with(requestHeaders(1L, "USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestPaymentDto(1L, 1L))))
        .andExpect(status().isCreated());

    Instant from = Instant.parse("2025-03-17T15:00:00Z");
    Instant to = Instant.parse("2027-03-17T15:00:00Z");

    mockMvc.perform(get("/api/v1/payments/summary/1")
            .with(requestHeaders(1L, "USER"))
            .param("from", String.valueOf(from))
            .param("to", String.valueOf(to)))
        .andExpect(status().isOk())
        .andExpect(content().string("50.50"));
  }

  @Test
  void getTotalSumForAllUsers_WhenPaymentsExist_ShouldReturnCorrectSum() throws Exception {
    stubRandomNumberApi(2L);
    mockMvc.perform(post("/api/v1/payments")
            .with(requestHeaders(1L, "USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestPaymentDto(1L, 1L))))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/v1/payments")
            .with(requestHeaders(2L, "USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestPaymentDto(2L, 2L))))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/v1/payments/summary")
            .with(requestHeaders(1L, "ADMIN")))
        .andExpect(status().isOk())
        .andExpect(content().string("101.00"));

  }

  @Test
  void getTotalSumForAllUsers_WhenNoPayments_ShouldReturnZero() throws Exception {
    mockMvc.perform(get("/api/v1/payments/summary")
            .with(requestHeaders(1L, "ADMIN")))
        .andExpect(status().isOk())
        .andExpect(content().string("0"));
  }
}