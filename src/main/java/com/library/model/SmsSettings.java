package com.library.model;

/**
 * Holds SMS provider settings loaded from/saved to local configuration.
 */
public class SmsSettings {

    private String provider;
    private String smsApiUrl;
    private String smsApiToken;
    private String smsSenderId;
    private String twilioAccountSid;
    private String twilioAuthToken;
    private String twilioFromNumber;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getSmsApiUrl() {
        return smsApiUrl;
    }

    public void setSmsApiUrl(String smsApiUrl) {
        this.smsApiUrl = smsApiUrl;
    }

    public String getSmsApiToken() {
        return smsApiToken;
    }

    public void setSmsApiToken(String smsApiToken) {
        this.smsApiToken = smsApiToken;
    }

    public String getSmsSenderId() {
        return smsSenderId;
    }

    public void setSmsSenderId(String smsSenderId) {
        this.smsSenderId = smsSenderId;
    }

    public String getTwilioAccountSid() {
        return twilioAccountSid;
    }

    public void setTwilioAccountSid(String twilioAccountSid) {
        this.twilioAccountSid = twilioAccountSid;
    }

    public String getTwilioAuthToken() {
        return twilioAuthToken;
    }

    public void setTwilioAuthToken(String twilioAuthToken) {
        this.twilioAuthToken = twilioAuthToken;
    }

    public String getTwilioFromNumber() {
        return twilioFromNumber;
    }

    public void setTwilioFromNumber(String twilioFromNumber) {
        this.twilioFromNumber = twilioFromNumber;
    }
}
