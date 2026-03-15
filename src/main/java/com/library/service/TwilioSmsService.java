package com.library.service;

import com.library.util.Constants;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Sends SMS via Twilio REST API.
 */
public class TwilioSmsService implements SmsService {

    private static final Logger LOGGER = Logger.getLogger(TwilioSmsService.class.getName());

    private final HttpClient httpClient;
    private final SmsSettingsService settingsService;

    public TwilioSmsService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.settingsService = SmsSettingsService.getInstance();
    }

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        String accountSid = settingsService.resolveSetting(Constants.TWILIO_ACCOUNT_SID_ENV);
        String authToken = settingsService.resolveSetting(Constants.TWILIO_AUTH_TOKEN_ENV);
        String fromNumber = settingsService.resolveSetting(Constants.TWILIO_FROM_NUMBER_ENV);

        if (isBlank(accountSid) || isBlank(authToken) || isBlank(fromNumber)) {
            LOGGER.warning("Twilio config missing. Set "
                    + Constants.TWILIO_ACCOUNT_SID_ENV + ", "
                    + Constants.TWILIO_AUTH_TOKEN_ENV + ", and "
                    + Constants.TWILIO_FROM_NUMBER_ENV + ".");
            return false;
        }

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
        String form = "To=" + encode(phoneNumber)
                + "&From=" + encode(fromNumber)
                + "&Body=" + encode(message);

        String credentials = accountSid + ":" + authToken;
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
            if (!success) {
                LOGGER.warning("Twilio rejected SMS for " + phoneNumber + ". Status: " + response.statusCode());
            }
            return success;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.warning("Twilio SMS failed for " + phoneNumber + ": " + e.getMessage());
            return false;
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
