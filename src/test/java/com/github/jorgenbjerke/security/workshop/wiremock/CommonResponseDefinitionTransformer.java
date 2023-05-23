package com.github.jorgenbjerke.security.workshop.wiremock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public abstract class CommonResponseDefinitionTransformer extends ResponseDefinitionTransformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public abstract void init(WireMockServer wireMockServer);

    @Override
    public String getName() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    @Override
    public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource fileSource, Parameters parameters) {
        String responseBody = toJsonString(getResponseForRequest(request));

        if (responseBody != null) {
            return ResponseDefinitionBuilder
                    .like(responseDefinition)
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody(responseBody)
                    .build();
        } else {
            return ResponseDefinitionBuilder
                    .like(responseDefinition)
                    .withStatus(HttpStatus.NOT_FOUND.value())
                    .build();
        }
    }

    public abstract Object getResponseForRequest(Request request);

    @SneakyThrows
    private String toJsonString(Object object) {
        return objectMapper.writeValueAsString(object);
    }
}
