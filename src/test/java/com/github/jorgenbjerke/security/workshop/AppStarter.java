package com.github.jorgenbjerke.security.workshop;

import com.github.jorgenbjerke.security.workshop.wiremock.WireMockConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@Slf4j
@SpringBootApplication
public class AppStarter {

    public static void main(String[] args) throws Exception {
        WireMockConfig wireMockConfig = new WireMockConfig();
        wireMockConfig.beforeAll(null);
        log.info("token: {}", WireMockConfig.issuerStub().generateToken());
        new SpringApplicationBuilder()
                .profiles("test")
                .sources(AppStarter.class)
                .run(args);
    }
}
