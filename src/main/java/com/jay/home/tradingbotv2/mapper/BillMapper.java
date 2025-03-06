package com.jay.home.tradingbotv2.mapper;

import com.jay.home.tradingbotv2.dto.BillDTO;
import com.jay.home.tradingbotv2.model.Bill;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BillMapper {

    /**
     * Convert a Bill entity to a BillDTO
     */
    public BillDTO toDTO(Bill bill) {
        if (bill == null) {
            return null;
        }
        
        BillDTO dto = new BillDTO();
        dto.setId(bill.getId());
        dto.setName(bill.getName());
        dto.setAmount(bill.getAmount());
        dto.setDueDay(bill.getDueDay());
        dto.setPaid(bill.isPaid());
        dto.setRecurring(bill.isRecurring());
        
        // Extract IDs and names from related entities
        if (bill.getUser() != null) {
            dto.setUserId(bill.getUser().getId());
        }
        
        if (bill.getCategory() != null) {
            dto.setCategoryId(bill.getCategory().getId());
            dto.setCategoryName(bill.getCategory().getName());
        }
        
        return dto;
    }
    
    /**
     * Convert a list of Bill entities to a list of BillDTOs
     */
    public List<BillDTO> toDTOList(List<Bill> bills) {
        return bills.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}