package com.vehiclecare.vehiclecaremicro.infrastructure.external.vpic;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class VpicClient {

    private static final String BASE_URL = "https://vpic.nhtsa.dot.gov/api/vehicles";
    private static final Logger logger = LoggerFactory.getLogger(VpicClient.class);

    private final RestTemplate restTemplate;

    public List<VpicMakeDTO> fetchMakes() {
        String url = BASE_URL + "/GetAllMakes?format=json";
        try {
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
        } catch (HttpServerErrorException ex) {
            logger.warn("vPIC returned {} for makes: {}", ex.getStatusCode(), ex.getMessage());
            return Collections.emptyList();
        } catch (RestClientException ex) {
            logger.warn("vPIC request failed for makes: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public List<VpicModelDTO> fetchModelsForMake(String makeId) {
        String url = BASE_URL + "/GetModelsForMakeId/" + makeId + "?format=json";
        try {
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
        } catch (HttpServerErrorException ex) {
            logger.warn("vPIC returned {} for models of make {}: {}", ex.getStatusCode(), makeId, ex.getMessage());
            return Collections.emptyList();
        } catch (RestClientException ex) {
            logger.warn("vPIC request failed for models of make {}: {}", makeId, ex.getMessage());
            return Collections.emptyList();
        }
    }
}
