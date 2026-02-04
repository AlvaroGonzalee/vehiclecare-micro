package com.vehiclecare.vehiclecaremicro.infrastructure.external.vpic;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class VpicClient {

    private static final String BASE_URL = "https://vpic.nhtsa.dot.gov/api/vehicles";

    private final RestTemplate restTemplate;

    public List<VpicMakeDTO> fetchMakes() {
        String url = BASE_URL + "/GetAllMakes?format=json";
        ResponseEntity<VpicResponse<VpicMakeDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        if (response.getBody() == null || response.getBody().getResults() == null) {
            return Collections.emptyList();
        }
        return response.getBody().getResults();
    }

    public List<VpicModelDTO> fetchModelsForMake(String makeId) {
        String url = BASE_URL + "/GetModelsForMakeId/" + makeId + "?format=json";
        ResponseEntity<VpicResponse<VpicModelDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        if (response.getBody() == null || response.getBody().getResults() == null) {
            return Collections.emptyList();
        }
        return response.getBody().getResults();
    }
}
