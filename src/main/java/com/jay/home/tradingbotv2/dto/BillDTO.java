package com.jay.home.tradingbotv2.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillDTO {
    private Long id;
    private String name;
    private BigDecimal amount;
    private int dueDay;
    private boolean isPaid;
    private boolean isRecurring;
    private Long userId;
    private Long categoryId;
    private String categoryName;
}