package com.github.jorgenbjerke.security.workshop;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

@Configuration
public class TestConfig {
    @Bean
    public BeanPostProcessor noRedirectingTestRestTemplatePostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof TestRestTemplate) {
                    ((TestRestTemplate) bean).getRestTemplate().setRequestFactory(
                            new SimpleClientHttpRequestFactory() {
                                @Override
                                protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                                    super.prepareConnection(connection, httpMethod);
                                    connection.setInstanceFollowRedirects(false);
                                }
                            }
                    );
                }
                return bean;
            }
        };
    }
}
