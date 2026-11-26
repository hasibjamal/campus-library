package edu.campus.library.infra.notification.vendor;

import java.io.PrintStream;
import java.util.Map;

/**
 * Simulated third-party mail SDK ("QuickMail"). Its API does not match our
 * {@code NotificationChannel}: it wants an address, an HTML body and a header
 * map, and answers with a status code. We do not change this class; we adapt it.
 */
public class QuickMailClient {
    private final PrintStream out;

    public QuickMailClient(PrintStream out) {
        this.out = out;
    }

    /** @return 202 when the message was accepted, 400 when the address is invalid. */
    public int post(String toAddress, String htmlBody, Map<String, String> headers) {
        if (toAddress == null || !toAddress.contains("@")) {
            return 400;
        }
        out.println("  [email to " + toAddress + "] " + headers.getOrDefault("Subject", "")
                + ": " + htmlBody.replaceAll("<[^>]+>", ""));
        return 202;
    }
}
