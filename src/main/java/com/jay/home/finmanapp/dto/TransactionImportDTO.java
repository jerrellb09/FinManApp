package com.jay.home.finmanapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionImportDTO {
    private LocalDate date;
    private LocalDate originalDate;
    private String accountType;
    private String accountName;
    private String accountNumber;
    private String institutionName;
    private String name;
    private String customName;
    private BigDecimal amount;
    private String description;
    private String category;
    private String note;
    private String ignoredFrom;
    private Boolean taxDeductible;
    
    public String getDisplayName() {
        if (customName != null && !customName.trim().isEmpty()) {
            return customName;
        }
        return name != null ? name : description;
    }
    
    public boolean isDebit() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isCredit() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    public BigDecimal getAbsoluteAmount() {
        return amount != null ? amount.abs() : BigDecimal.ZERO;
    }
}