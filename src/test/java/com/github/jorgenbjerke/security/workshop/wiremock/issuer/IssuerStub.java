package com.github.jorgenbjerke.security.workshop.wiremock.issuer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jorgenbjerke.security.workshop.wiremock.CommonResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Request;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.time.Instant.now;


@Getter
public class IssuerStub extends CommonResponseDefinitionTransformer {
    private final RSAKey key;
    private final RSASSASigner signer;
    private final JWSHeader header;

    @SneakyThrows
    public IssuerStub() {
        key = new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate();
        signer = new RSASSASigner(key);
        header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build();
    }

    @Override
    public void init(WireMockServer wireMockServer) {
        wireMockServer.stubFor(get(urlPathMatching("/issuer/(.*)"))
                .willReturn(aResponse().withTransformers(getName()))
        );
    }

    @Override
    public Object getResponseForRequest(Request request) {
        return switch (request.getUrl()) {
            case "/issuer/.well-known/openid-configuration" -> createWellKnownConfigurationResponse();
            case "/issuer/jwks" -> createJwksResponse();
            default -> null;
        };
    }

    private WellKnownConfigurationResponse createWellKnownConfigurationResponse() {
        String baseUrl = "http://localhost:" + getPort() + "/issuer";
        return new WellKnownConfigurationResponse.WellKnownConfigurationResponseBuilder()
                .issuer(baseUrl)
                .jwksUri(baseUrl + "/jwks")
                .subjectTypesSuppoerted(List.of("pairwise"))
                .authorizationEndpoint(baseUrl + "/authorize")
                .tokenEndpoint(baseUrl + "/token")
                .build();
    }

    private JwksResponse createJwksResponse() {
        return new JwksResponse(
                List.of(key.toPublicJWK().toJSONObject())
        );
    }

    private String getPort() {
        return System.getProperty("wiremock.server.port");
    }

    public String generateToken() {
        Map<String, Object> claims = createCommonClaims();
        return generateToken(claims);
    }

    public String generateToken(Consumer<Map<String, Object>> claimsEditor) {
        Map<String, Object> claims = createCommonClaims();
        claimsEditor.accept(claims);
        return generateToken(claims);
    }

    private Map<String, Object> createCommonClaims() {
        Instant now = now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("aud", "our-app-client-id");
        claims.put("iss", "http://localhost:" + getPort() + "/issuer");
        claims.put("iat", now.minusSeconds(60).getEpochSecond());
        claims.put("nbf", now.minusSeconds(60).getEpochSecond());
        claims.put("exp", now.plusSeconds(3600).getEpochSecond());
        claims.put("ver", "2.0");
        return claims;
    }

    @SneakyThrows
    private String generateToken(Map<String, Object> claims) {
        JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder();
        claims.forEach(claimsSetBuilder::claim);

        SignedJWT signedToken = new SignedJWT(header, claimsSetBuilder.build());
        signedToken.sign(signer);
        return signedToken.serialize();
    }

    @Getter
    @Builder
    private static class WellKnownConfigurationResponse {
        @JsonProperty("issuer")
        private String issuer;

        @JsonProperty("jwks_uri")
        private String jwksUri;

        @JsonProperty("subject_types_supported")
        private List<String> subjectTypesSuppoerted;

        @JsonProperty("authorization_endpoint")
        private String authorizationEndpoint;

        @JsonProperty("token_endpoint")
        private String tokenEndpoint;
    }

    private record JwksResponse(List<Map<String, Object>> keys) {
    }

}
