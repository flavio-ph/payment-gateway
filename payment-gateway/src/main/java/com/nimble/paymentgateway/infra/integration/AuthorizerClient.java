package com.nimble.paymentgateway.infra.integration;

import com.nimble.paymentgateway.domain.exception.PagamentoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class AuthorizerClient {

    private final RestTemplate restTemplate;
    private final String authorizerUrl;

    public AuthorizerClient(RestTemplate restTemplate, @Value("${external.authorizer.url}") String authorizerUrl) {
        this.restTemplate = restTemplate;
        this.authorizerUrl = authorizerUrl;
    }

    public boolean authorize() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(authorizerUrl, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {

                return true;
            }
            return false;
        } catch (Exception e) {

            throw new PagamentoException("Serviço autorizador externo indisponível.");
        }
    }
}