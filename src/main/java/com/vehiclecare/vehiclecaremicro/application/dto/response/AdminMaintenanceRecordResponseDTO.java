package com.vehiclecare.vehiclecaremicro.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminMaintenanceRecordResponseDTO {
    private String id;
    private String userId;
    private String vehicleId;
    private String title;
    private LocalDate date;
    private String category;
    private Integer kilometers;
    private BigDecimal price;
    private String description;
    private List<AttachmentResponseDTO> attachments;
}
