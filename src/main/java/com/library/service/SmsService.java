package com.library.service;

/**
 * Abstraction for SMS delivery providers.
 */
public interface SmsService {

    /**
     * Sends an SMS message.
     *
     * @return true when the provider accepted the message.
     */
    boolean sendSms(String phoneNumber, String message);
}
