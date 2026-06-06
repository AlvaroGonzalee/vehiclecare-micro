package com.vehiclecare.vehiclecaremicro.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain model describing a maintenance event associated with a vehicle.
 *
 * <p>A maintenance record stores both operational and financial information about
 * a service action, such as when it happened, the category, odometer value, price
 * and optional supporting attachments. The object is intentionally free from
 * infrastructure concerns so it can flow across use cases and adapters.</p>
 */
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
