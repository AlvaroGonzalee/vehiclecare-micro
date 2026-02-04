package com.vehiclecare.vehiclecaremicro.infrastructure.external.vpic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VpicMakeDTO {
    @JsonProperty("Make_ID")
    private String id;

    @JsonProperty("Make_Name")
    private String name;
}
