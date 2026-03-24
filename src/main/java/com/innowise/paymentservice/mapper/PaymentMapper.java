package com.innowise.paymentservice.mapper;

import com.innowise.paymentservice.model.dto.RequestPaymentDto;
import com.innowise.paymentservice.model.dto.ResponsePaymentDto;
import com.innowise.paymentservice.model.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

  Payment toEntity(RequestPaymentDto dto);

  ResponsePaymentDto toDto(Payment entity);
}
