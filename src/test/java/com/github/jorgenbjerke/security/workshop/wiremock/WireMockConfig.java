package com.github.jorgenbjerke.security.workshop.wiremock;

import com.github.jorgenbjerke.security.workshop.wiremock.issuer.IssuerStub;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Slf4j
@Configuration
public class WireMockConfig implements BeforeAllCallback {
    @Getter(onMethod = @__(@Bean))
    private final static IssuerStub issuerStub = new IssuerStub();
    private final static CommonResponseDefinitionTransformer[] transformers = new CommonResponseDefinitionTransformer[]{
            issuerStub
    };
    @Getter(onMethod = @__(@Bean))
    private final static WireMockServer wireMockServer = new WireMockServer(
            wireMockConfig()
                    .port(0)
                    .extensions(transformers)
    );

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (wireMockServer.isRunning()) {
            return;
        }
        log.info("starting WireMock server");
        wireMockServer.start();
        System.setProperty("wiremock.server.port", "" + wireMockServer.port());
        Arrays.stream(transformers).forEach(t -> t.init(wireMockServer));
        log.info("WireMock server started on port {}", wireMockServer.port());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("stopping WireMock server");
            wireMockServer.stop();
        }));
    }
}
