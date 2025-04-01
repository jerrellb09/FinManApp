package com.jay.home.finmanapp.mapper;

import com.jay.home.finmanapp.dto.BillDTO;
import com.jay.home.finmanapp.model.Bill;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between Bill entities and BillDTO objects.
 * 
 * This class handles the transformation of data between the internal entity representation
 * (Bill) and the data transfer object (BillDTO) used for API communication. The mapper
 * ensures that only the necessary data is transferred, handles related entities appropriately,
 * and prevents circular references.
 * 
 * This separation of concerns helps maintain a clean architecture by keeping entity
 * classes focused on domain logic and persistence, while DTOs focus on data transfer
 * and presentation needs.
 */
@Component
public class BillMapper {

    /**
     * Convert a Bill entity to a BillDTO.
     * 
     * This method transforms a Bill entity into its corresponding DTO representation,
     * handling nested entities by extracting only the necessary identifiers and data.
     * It prevents circular references and excessive data transfer.
     *
     * @param bill The Bill entity to convert
     * @return A BillDTO containing the bill data, or null if input is null
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
     * Convert a list of Bill entities to a list of BillDTOs.
     * 
     * This method applies the toDTO conversion to each bill in the provided list,
     * resulting in a list of DTOs suitable for API responses or other data transfer needs.
     *
     * @param bills The list of Bill entities to convert
     * @return A list of corresponding BillDTOs
     */
    public List<BillDTO> toDTOList(List<Bill> bills) {
        return bills.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}