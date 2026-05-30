package com.pricewatch.client;

import com.pricewatch.dto.ExternalDtos.EvolutionSendTextRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
public class WhatsAppClient {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String instance;
    private final String apiKey;

    public WhatsAppClient(
            RestTemplate restTemplate,
            @Value("${evolution.api-url}") String apiUrl,
            @Value("${evolution.instance}") String instance,
            @Value("${evolution.api-key}") String apiKey
    ) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.instance = instance;
        this.apiKey = apiKey;
    }

    public String sendText(String phone, String text) {
        if (!StringUtils.hasText(apiUrl) || !StringUtils.hasText(instance) || !StringUtils.hasText(apiKey)) {
            return "SKIPPED_EVOLUTION_NOT_CONFIGURED";
        }

        String url = apiUrl + "/message/sendText/" + instance;
        String normalizedPhone = phone.replaceAll("\\D", "");
        String number = normalizedPhone.startsWith("55") ? normalizedPhone : "55" + normalizedPhone;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", apiKey);

        EvolutionSendTextRequest body = new EvolutionSendTextRequest(number, text);
        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        return "SENT";
    }
}
