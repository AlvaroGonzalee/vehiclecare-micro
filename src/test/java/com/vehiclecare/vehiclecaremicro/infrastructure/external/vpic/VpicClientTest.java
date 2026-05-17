package com.vehiclecare.vehiclecaremicro.infrastructure.external.vpic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class VpicClientTest {

    private static final String MAKES_URL =
            "https://vpic.nhtsa.dot.gov/api/vehicles/GetAllMakes?format=json";
    private static final String MODELS_URL =
            "https://vpic.nhtsa.dot.gov/api/vehicles/GetModelsForMakeId/10?format=json";

    @Mock
    private RestTemplate restTemplate;

    private VpicClient client;

    @BeforeEach
    void setUp() {
        client = new VpicClient(restTemplate);
    }

    @Test
    void fetchMakes_returnsResultsWhenBodyPresent() {
        VpicMakeDTO make = new VpicMakeDTO();
        make.setId("1");
        make.setName("BMW");
        VpicResponse<VpicMakeDTO> body = new VpicResponse<>();
        body.setResults(List.of(make));
        when(restTemplate.exchange(
                eq(MAKES_URL),
                eq(HttpMethod.GET),
                eq(null),
                anyMakesResponseType()
        )).thenReturn(ResponseEntity.ok(body));

        List<VpicMakeDTO> result = client.fetchMakes();

        assertEquals(1, result.size());
        assertEquals("BMW", result.get(0).getName());
    }

    @Test
    void fetchMakes_returnsEmptyOnNullBodyOrResultsOrErrors() {
        when(restTemplate.exchange(
                eq(MAKES_URL),
                eq(HttpMethod.GET),
                eq(null),
                anyMakesResponseType()
        )).thenReturn(ResponseEntity.ok(null));

        assertTrue(client.fetchMakes().isEmpty());

        VpicResponse<VpicMakeDTO> body = new VpicResponse<>();
        body.setResults(null);
        when(restTemplate.exchange(
                eq(MAKES_URL),
                eq(HttpMethod.GET),
                eq(null),
                anyMakesResponseType()
        )).thenReturn(ResponseEntity.ok(body));
        assertTrue(client.fetchMakes().isEmpty());

        when(restTemplate.exchange(
                eq(MAKES_URL),
                eq(HttpMethod.GET),
                eq(null),
                anyMakesResponseType()
        )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        assertTrue(client.fetchMakes().isEmpty());

        when(restTemplate.exchange(
                eq(MAKES_URL),
                eq(HttpMethod.GET),
                eq(null),
                anyMakesResponseType()
        )).thenThrow(new RestClientException("boom"));
        assertTrue(client.fetchMakes().isEmpty());
    }

    @Test
    void fetchModelsForMake_returnsResultsAndHandlesErrors() {
        VpicModelDTO model = new VpicModelDTO();
        model.setId("m1");
        model.setName("320i");
        VpicResponse<VpicModelDTO> body = new VpicResponse<>();
        body.setResults(List.of(model));
        when(restTemplate.exchange(
                eq(MODELS_URL),
                eq(HttpMethod.GET),
                eq(null),
                anyModelsResponseType()
        )).thenReturn(ResponseEntity.ok(body));

        List<VpicModelDTO> result = client.fetchModelsForMake("10");

        assertEquals(1, result.size());
        assertEquals("320i", result.get(0).getName());

        when(restTemplate.exchange(
                eq(MODELS_URL),
                eq(HttpMethod.GET),
                eq(null),
                anyModelsResponseType()
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));
        assertTrue(client.fetchModelsForMake("10").isEmpty());

        VpicResponse<VpicModelDTO> nullResults = new VpicResponse<>();
        when(restTemplate.exchange(
                eq(MODELS_URL),
                eq(HttpMethod.GET),
                eq(null),
                anyModelsResponseType()
        )).thenReturn(ResponseEntity.ok(nullResults));
        assertTrue(client.fetchModelsForMake("10").isEmpty());

        when(restTemplate.exchange(
                eq(MODELS_URL),
                eq(HttpMethod.GET),
                eq(null),
                anyModelsResponseType()
        )).thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));
        assertTrue(client.fetchModelsForMake("10").isEmpty());

        when(restTemplate.exchange(
                eq(MODELS_URL),
                eq(HttpMethod.GET),
                eq(null),
                anyModelsResponseType()
        )).thenThrow(new RestClientException("boom"));
        assertTrue(client.fetchModelsForMake("10").isEmpty());
    }

    private ParameterizedTypeReference<VpicResponse<VpicMakeDTO>> anyMakesResponseType() {
        return any();
    }

    private ParameterizedTypeReference<VpicResponse<VpicModelDTO>> anyModelsResponseType() {
        return any();
    }
}
