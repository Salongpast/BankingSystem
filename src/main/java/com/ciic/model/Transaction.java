package com.ciic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private Long id;
    private String transactionReference;
    private String accountNumber;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal balance;
    private String referenceAccount;
    private LocalDateTime createdAt;
}
