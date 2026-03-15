package com.library.service;

import com.library.util.Constants;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * Sends SMS using a generic HTTP JSON gateway configured with environment variables.
 */
public class ConfigurableSmsService implements SmsService {

    private static final Logger LOGGER = Logger.getLogger(ConfigurableSmsService.class.getName());

    private final HttpClient httpClient;
    private final SmsSettingsService settingsService;

    public ConfigurableSmsService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.settingsService = SmsSettingsService.getInstance();
    }

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        String apiUrl = settingsService.resolveSetting(Constants.SMS_API_URL_ENV);
        String token = settingsService.resolveSetting(Constants.SMS_API_TOKEN_ENV);
        String senderId = settingsService.resolveSetting(Constants.SMS_SENDER_ID_ENV);

        if (apiUrl == null || apiUrl.isBlank()) {
            LOGGER.warning("SMS API URL not configured. Set " + Constants.SMS_API_URL_ENV + " to enable SMS alerts.");
            return false;
        }

        String payload = String.format(
                "{\"to\":\"%s\",\"message\":\"%s\",\"sender\":\"%s\"}",
                escapeJson(phoneNumber),
                escapeJson(message),
                escapeJson(senderId == null ? "LIBRARY" : senderId));

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));

        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }

        try {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
            if (!success) {
                LOGGER.warning("SMS provider rejected message for " + phoneNumber + ". Status: " + response.statusCode());
            }
            return success;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.warning("Failed to send SMS to " + phoneNumber + ": " + e.getMessage());
            return false;
        }
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
