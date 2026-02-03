package com.vehiclecare.vehiclecaremicro.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class MaintenanceRecord {
    private String id;
    private String vehicleId;
    private String title;
    private LocalDate date;
    private String category;
    private Integer kilometers;
    private BigDecimal price;
    private String description;
    private List<Attachment> attachments;
}
