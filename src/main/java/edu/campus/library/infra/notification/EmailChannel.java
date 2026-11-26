package edu.campus.library.infra.notification;

import edu.campus.library.domain.member.Member;
import edu.campus.library.infra.notification.vendor.QuickMailClient;
import edu.campus.library.service.NotificationChannel;

import java.util.Map;

/**
 * Adapter (L4): makes the vendor {@link QuickMailClient} look like our
 * {@link NotificationChannel}. The service layer never sees the vendor API,
 * so switching mail providers means writing another adapter.
 */
public class EmailChannel implements NotificationChannel {
    private final QuickMailClient client;

    public EmailChannel(QuickMailClient client) {
        this.client = client;
    }

    @Override
    public String name() {
        return "email";
    }

    @Override
    public void send(Member to, String subject, String body) {
        int status = client.post(to.email(), "<p>" + escape(body) + "</p>",
                Map.of("Subject", subject, "X-Library", "campus"));
        if (status != 202) {
            throw new IllegalStateException("Mail provider refused message for " + to.id() + " (" + status + ")");
        }
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
