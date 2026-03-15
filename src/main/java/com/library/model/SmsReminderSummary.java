package com.library.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates the result of an SMS reminder run.
 */
public class SmsReminderSummary {

    private int attempted;
    private int sent;
    private int failed;
    private int skippedNoPhone;
    private int skippedOutsideWindow;
    private final List<String> details = new ArrayList<>();

    public int getAttempted() {
        return attempted;
    }

    public void setAttempted(int attempted) {
        this.attempted = attempted;
    }

    public int getSent() {
        return sent;
    }

    public void setSent(int sent) {
        this.sent = sent;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getSkippedNoPhone() {
        return skippedNoPhone;
    }

    public void setSkippedNoPhone(int skippedNoPhone) {
        this.skippedNoPhone = skippedNoPhone;
    }

    public int getSkippedOutsideWindow() {
        return skippedOutsideWindow;
    }

    public void setSkippedOutsideWindow(int skippedOutsideWindow) {
        this.skippedOutsideWindow = skippedOutsideWindow;
    }

    public List<String> getDetails() {
        return Collections.unmodifiableList(details);
    }

    public void addDetail(String detail) {
        details.add(detail);
    }
}
