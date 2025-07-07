package com.jay.home.finmanapp.service;

import com.jay.home.finmanapp.dto.TransactionImportDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CSVParserService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public List<TransactionImportDTO> parseCSV(MultipartFile file) throws IOException {
        List<TransactionImportDTO> transactions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build();
            
            CSVParser parser = csvFormat.parse(reader);
            
            for (CSVRecord record : parser) {
                try {
                    TransactionImportDTO transaction = parseRecord(record);
                    transactions.add(transaction);
                } catch (Exception e) {
                    log.warn("Failed to parse CSV record at line {}: {}", record.getRecordNumber(), e.getMessage());
                }
            }
        }
        
        log.info("Successfully parsed {} transactions from CSV file", transactions.size());
        return transactions;
    }
    
    private TransactionImportDTO parseRecord(CSVRecord record) {
        TransactionImportDTO transaction = new TransactionImportDTO();
        
        transaction.setDate(parseDate(record.get("Date")));
        transaction.setOriginalDate(parseDate(record.get("Original Date")));
        transaction.setAccountType(record.get("Account Type"));
        transaction.setAccountName(record.get("Account Name"));
        transaction.setAccountNumber(record.get("Account Number"));
        transaction.setInstitutionName(record.get("Institution Name"));
        transaction.setName(record.get("Name"));
        transaction.setCustomName(record.get("Custom Name"));
        transaction.setAmount(parseAmount(record.get("Amount")));
        transaction.setDescription(record.get("Description"));
        transaction.setCategory(record.get("Category"));
        transaction.setNote(record.get("Note"));
        transaction.setIgnoredFrom(record.get("Ignored From"));
        transaction.setTaxDeductible(parseBoolean(record.get("Tax Deductible")));
        
        return transaction;
    }
    
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }
    
    private BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        try {
            String cleanAmount = amountStr.trim().replace(",", "");
            return new BigDecimal(cleanAmount);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse amount: {}", amountStr);
            return BigDecimal.ZERO;
        }
    }
    
    private Boolean parseBoolean(String boolStr) {
        if (boolStr == null || boolStr.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = boolStr.trim().toLowerCase();
        return "true".equals(trimmed) || "yes".equals(trimmed) || "1".equals(trimmed);
    }
}