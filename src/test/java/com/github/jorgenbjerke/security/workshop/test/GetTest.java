package com.github.jorgenbjerke.security.workshop.test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GET api/v1")
public class GetTest extends SecuredEndpointTest {
    private static final String ENDPOINT_URL = "/api/v1";

    @Override
    protected RequestEntity<?> getRequestEntity() {
        return RequestEntity.get(ENDPOINT_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + issuerStub.generateToken())
                .build();
    }

    @Test
    @DisplayName("Happy path")
    void happyPath() {
        RequestEntity<?> request = getRequestEntity();
        ResponseEntity<JsonNode> responseEntity = testRestTemplate.exchange(request, JsonNode.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertEquals("hello world", responseEntity.getBody().get("value").asText());
    }
}
