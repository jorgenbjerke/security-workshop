package com.github.jorgenbjerke.security.workshop;

import com.github.jorgenbjerke.security.workshop.wiremock.WireMockConfig;
import com.github.jorgenbjerke.security.workshop.wiremock.issuer.IssuerStub;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;


@ExtendWith(WireMockConfig.class)
@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public abstract class TestBase {
    @Autowired
    protected IssuerStub issuerStub;
    @Autowired
    protected WireMockServer wireMockServer;
    @Autowired
    protected TestRestTemplate testRestTemplate;
}
