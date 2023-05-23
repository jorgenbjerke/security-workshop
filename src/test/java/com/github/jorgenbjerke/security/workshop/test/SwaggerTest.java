package com.github.jorgenbjerke.security.workshop.test;

import com.github.jorgenbjerke.security.workshop.TestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Swagger UI")
public class SwaggerTest extends TestBase {

    @Test
    @DisplayName("swagger-ui/**")
    public void test1() {
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/swagger-ui/index.html", String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("swagger-ui.html")
    public void test2() {
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/swagger-ui.html", String.class);
        assertEquals(HttpStatus.FOUND, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("v3/api-docs/**")
    public void test3() {
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/v3/api-docs/swagger-config", String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
