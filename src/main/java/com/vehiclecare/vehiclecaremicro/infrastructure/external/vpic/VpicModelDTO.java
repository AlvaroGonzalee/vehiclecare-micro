package com.vehiclecare.vehiclecaremicro.infrastructure.external.vpic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VpicModelDTO {
    @JsonProperty("Model_ID")
    private String id;

    @JsonProperty("Model_Name")
    private String name;

    @JsonProperty("Make_ID")
    private String makeId;
}
