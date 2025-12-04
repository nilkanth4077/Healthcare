package com.healthcare.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ZoomAuthService {

    @Value("${zoom.client.id}")
    private String clientId;

    @Value("${zoom.client.secret}")
    private String clientSecret;

    @Value("${zoom.account.id}")
    private String accountId;

    public String generateAccessToken() {
        String url = "https://zoom.us/oauth/token"
                + "?grant_type=account_credentials&account_id=" + accountId;

        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);

        HttpEntity<String> request = new HttpEntity<>(headers);
        Map<String, Object> response = rest.postForObject(url, request, Map.class);

        return response.get("access_token").toString();
    }
}