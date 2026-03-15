package com.library.service;

import com.library.model.SmsSettings;
import com.library.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Persists and resolves SMS settings from environment variables and local config.
 */
public class SmsSettingsService {

    private static final Logger LOGGER = Logger.getLogger(SmsSettingsService.class.getName());
    private static final String SMS_SETTINGS_PATH = "data/sms.properties";

    private static volatile SmsSettingsService instance;

    private SmsSettingsService() {
    }

    public static SmsSettingsService getInstance() {
        if (instance == null) {
            synchronized (SmsSettingsService.class) {
                if (instance == null) {
                    instance = new SmsSettingsService();
                }
            }
        }
        return instance;
    }

    public SmsSettings loadSettings() {
        Properties props = loadProperties();

        SmsSettings settings = new SmsSettings();
        settings.setProvider(props.getProperty(Constants.SMS_PROVIDER_ENV, ""));
        settings.setSmsApiUrl(props.getProperty(Constants.SMS_API_URL_ENV, ""));
        settings.setSmsApiToken(props.getProperty(Constants.SMS_API_TOKEN_ENV, ""));
        settings.setSmsSenderId(props.getProperty(Constants.SMS_SENDER_ID_ENV, ""));
        settings.setTwilioAccountSid(props.getProperty(Constants.TWILIO_ACCOUNT_SID_ENV, ""));
        settings.setTwilioAuthToken(props.getProperty(Constants.TWILIO_AUTH_TOKEN_ENV, ""));
        settings.setTwilioFromNumber(props.getProperty(Constants.TWILIO_FROM_NUMBER_ENV, ""));

        return settings;
    }

    public void saveSettings(SmsSettings settings) {
        Properties props = new Properties();

        putIfPresent(props, Constants.SMS_PROVIDER_ENV, settings.getProvider());
        putIfPresent(props, Constants.SMS_API_URL_ENV, settings.getSmsApiUrl());
        putIfPresent(props, Constants.SMS_API_TOKEN_ENV, settings.getSmsApiToken());
        putIfPresent(props, Constants.SMS_SENDER_ID_ENV, settings.getSmsSenderId());
        putIfPresent(props, Constants.TWILIO_ACCOUNT_SID_ENV, settings.getTwilioAccountSid());
        putIfPresent(props, Constants.TWILIO_AUTH_TOKEN_ENV, settings.getTwilioAuthToken());
        putIfPresent(props, Constants.TWILIO_FROM_NUMBER_ENV, settings.getTwilioFromNumber());

        try {
            Path configPath = Path.of(SMS_SETTINGS_PATH);
            Path parent = configPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream outputStream = Files.newOutputStream(configPath)) {
                props.store(outputStream, "Library SMS settings");
            }
            LOGGER.info("SMS settings saved to " + SMS_SETTINGS_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save SMS settings: " + e.getMessage(), e);
        }
    }

    /**
     * Returns value from environment first, then from local settings file.
     */
    public String resolveSetting(String key) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        Properties props = loadProperties();
        String fileValue = props.getProperty(key);
        return fileValue == null ? null : fileValue.trim();
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        Path configPath = Path.of(SMS_SETTINGS_PATH);
        if (!Files.exists(configPath)) {
            return props;
        }

        try (InputStream inputStream = Files.newInputStream(configPath)) {
            props.load(inputStream);
        } catch (IOException e) {
            LOGGER.warning("Unable to load SMS settings file: " + e.getMessage());
        }

        return props;
    }

    private void putIfPresent(Properties props, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            props.setProperty(key, value.trim());
        }
    }
}
