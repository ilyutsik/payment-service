package com.innowise.paymentservice.repository.impl;

import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import com.innowise.paymentservice.model.entity.SumOfPayments;
import com.innowise.paymentservice.repository.PaymentCustomRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentCustomRepositoryImpl implements PaymentCustomRepository {

  private final MongoTemplate mongoTemplate;

  @Override
  public List<Payment> findByUserIdOrOrderIdOrStatus(Long userId, Long orderId,
      PaymentStatus status) {

    Query query = new Query();

    if (userId != null) {
      query.addCriteria(Criteria.where("user_id").is(userId));
    }
    if (orderId != null) {
      query.addCriteria(Criteria.where("order_id").is(orderId));
    }
    if (status != null) {
      query.addCriteria(Criteria.where("status").is(status));
    }

    return mongoTemplate.find(query, Payment.class);
  }

  @Override
  public BigDecimal getTotalPaymentsForUser(Long userId, Instant from, Instant to) {
    List<Criteria> criteriaList = criteriaDateFromTo(from, to);
    criteriaList.add(Criteria.where("user_id").is(userId));

    Aggregation aggregation = Aggregation.newAggregation(
        Aggregation.match(new Criteria().andOperator(criteriaList)),
        Aggregation.group().sum("payment_amount").as("sumOfPayments"));

    AggregationResults<SumOfPayments> results = mongoTemplate.aggregate(aggregation,
        mongoTemplate.getCollectionName(Payment.class), SumOfPayments.class);
    return Optional.ofNullable(results.getUniqueMappedResult())
        .map(SumOfPayments::sumOfPayments).orElse(BigDecimal.ZERO);
  }

  @Override
  public BigDecimal getTotalPaymentsForAll(Instant from, Instant to) {
    List<Criteria> criteriaList = criteriaDateFromTo(from, to);

    List<AggregationOperation> operations = new ArrayList<>();

    if (!criteriaList.isEmpty()) {
      operations.add(Aggregation.match(new Criteria().andOperator(criteriaList)));
    }
    operations.add(Aggregation.group().sum("payment_amount").as("sumOfPayments"));

    Aggregation aggregation = Aggregation.newAggregation(operations);

    AggregationResults<SumOfPayments> results = mongoTemplate.aggregate(aggregation,
        mongoTemplate.getCollectionName(Payment.class), SumOfPayments.class);
    return Optional.ofNullable(results.getUniqueMappedResult())
        .map(SumOfPayments::sumOfPayments).orElse(BigDecimal.ZERO);
  }

  private List<Criteria> criteriaDateFromTo(Instant from, Instant to) {
    final String timestamp = "timestamp";
    List<Criteria> criteriaList = new ArrayList<>();
    if (from != null && to != null) {
      criteriaList.add(Criteria.where(timestamp).gte(from).lte(to));
    } else if (from != null) {
      criteriaList.add(Criteria.where(timestamp).gte(from));
    } else if (to != null) {
      criteriaList.add(Criteria.where(timestamp).lte(to));
    }
    return criteriaList;
  }
}
