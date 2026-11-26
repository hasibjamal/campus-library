package edu.campus.library.infra.notification;

import edu.campus.library.domain.member.Member;
import edu.campus.library.service.NotificationChannel;

import java.io.PrintStream;

/**
 * Added after EmailChannel as a new class only, to prove the notification
 * extension point. Simulated: prints a short text message.
 */
public class SmsChannel implements NotificationChannel {
    private final PrintStream out;

    public SmsChannel(PrintStream out) {
        this.out = out;
    }

    @Override
    public String name() {
        return "sms";
    }

    @Override
    public void send(Member to, String subject, String body) {
        String text = subject + ": " + body;
        if (text.length() > 160) {
            text = text.substring(0, 157) + "...";
        }
        out.println("  [sms to " + to.name() + "] " + text);
    }
}
