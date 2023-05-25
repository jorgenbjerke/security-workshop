package com.github.jorgenbjerke.security.workshop.test;

import com.github.jorgenbjerke.security.workshop.TestBase;
import com.github.jorgenbjerke.security.workshop.wiremock.issuer.IssuerStub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.ReflectionUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class SecuredEndpointTest extends TestBase {

    protected abstract RequestEntity<?> getRequestEntity();

    @Test
    @DisplayName("The Authorization header is missing")
    void testMissingAuthHeader() {
        RequestEntity<?> request = getRequestEntity();
        removeAuthorizationHeader(request);
        ResponseEntity<byte[]> response = testRestTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("The Authorization signature is invalid")
    void testBadSignature1() {
        RequestEntity<?> request = getRequestEntity();
        String[] originalAuthHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0).split("[.]");
        String[] newAuthHeader = issuerStub.generateToken(claims -> claims.replace("iat", Instant.now().minusSeconds(7200).getEpochSecond())).split("[.]");
        newAuthHeader[2] = originalAuthHeader[2];
        replaceAuthorizationHeader(request, Arrays.stream(newAuthHeader).collect(Collectors.joining(".")));
        ResponseEntity<byte[]> response = testRestTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("The Authorization token is signed by another issuer")
    void testBadSignature2() {
        RequestEntity<?> request = getRequestEntity();
        replaceAuthorizationHeader(request, new IssuerStub().generateToken());
        ResponseEntity<byte[]> response = testRestTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("The audience claim is missing")
    void testAud1() {
        RequestEntity<?> request = getRequestEntity();
        replaceAuthorizationHeader(request, issuerStub.generateToken(claims -> claims.remove("aud")));
        ResponseEntity<byte[]> response = testRestTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("The audience claim refers to another application")
    void testAud2() {
        RequestEntity<?> request = getRequestEntity();
        replaceAuthorizationHeader(request, issuerStub.generateToken(claims -> claims.replace("aud", "not-our-app-client-id")));
        ResponseEntity<byte[]> response = testRestTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("The issuer claim is missing")
    void testIss1() {
        RequestEntity<?> request = getRequestEntity();
        replaceAuthorizationHeader(request, issuerStub.generateToken(claims -> claims.remove("iss")));
        ResponseEntity<byte[]> response = testRestTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Unknown issuer")
    void testIss2() {
        RequestEntity<?> request = getRequestEntity();
        replaceAuthorizationHeader(request, issuerStub.generateToken(claims -> claims.replace("iss", "unknown-issuer")));
        ResponseEntity<byte[]> response = testRestTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    //if the issued-at claim is missing, spring security defaults it to the expiration-time claim. Don't ask why...
    @Test
    @DisplayName("The issued-at claim is missing")
    void testIat1() {
        RequestEntity<?> request = getRequestEntity();
        replaceAuthorizationHeader(request, issuerStub.generateToken(claims -> claims.remove("iat")));
        ResponseEntity<byte[]> response = testRestTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("The issued-at claim is in the future")
    void testIat2() {
        RequestEntity<?> request = getRequestEntity();
        replaceAuthorizationHeader(request, issuerStub.generateToken(claims -> claims.replace("iat", now().plusSeconds(60).getEpochSecond())));
        ResponseEntity<byte[]> response = testRestTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("The not-before claim is missing")
    void testNbf1() {
        RequestEntity<?> request = getRequestEntity();
        replaceAuthorizationHeader(request, issuerStub.generateToken(claims -> claims.remove("nbf")));
        ResponseEntity<byte[]> response = testRestTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("The not-before claim is in the future")
    void testNbf2() {
        RequestEntity<?> request = getRequestEntity();
        replaceAuthorizationHeader(request, issuerStub.generateToken(claims -> claims.replace("nbf", now().plusSeconds(60).getEpochSecond())));
        ResponseEntity<byte[]> response = testRestTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("The expiration-time claim is missing")
    void testExp1() {
        RequestEntity<?> request = getRequestEntity();
        replaceAuthorizationHeader(request, issuerStub.generateToken(claims -> claims.remove("exp")));
        ResponseEntity<byte[]> response = testRestTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("The expiration-time claim is in the past")
    void testExp2() {
        RequestEntity<?> request = getRequestEntity();
        replaceAuthorizationHeader(request, issuerStub.generateToken(claims -> claims.replace("exp", now().minusSeconds(60).getEpochSecond())));
        ResponseEntity<byte[]> response = testRestTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private void removeAuthorizationHeader(RequestEntity<?> request) {
        LinkedMultiValueMap<String, String> newHeaders = new LinkedMultiValueMap<>(request.getHeaders());
        newHeaders.remove(HttpHeaders.AUTHORIZATION);
        ReflectionUtils.makeAccessible(ReflectionUtils.findField(RequestEntity.class, "headers"));
        ReflectionUtils.setField(ReflectionUtils.findField(RequestEntity.class, "headers"), request, new HttpHeaders(newHeaders));
    }

    private void replaceAuthorizationHeader(RequestEntity<?> request, String newToken) {
        LinkedMultiValueMap<String, String> newHeaders = new LinkedMultiValueMap<>(request.getHeaders());
        newHeaders.replace(HttpHeaders.AUTHORIZATION, List.of("Bearer " + newToken));
        ReflectionUtils.makeAccessible(ReflectionUtils.findField(RequestEntity.class, "headers"));
        ReflectionUtils.setField(ReflectionUtils.findField(RequestEntity.class, "headers"), request, new HttpHeaders(newHeaders));
    }
}
