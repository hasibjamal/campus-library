package edu.campus.library.infra.notification;

import edu.campus.library.domain.member.Member;
import edu.campus.library.service.NotificationChannel;

import java.io.PrintStream;

/** Simulated e-mail: writes the message to the console instead of an SMTP server. */
public class EmailChannel implements NotificationChannel {
    private final PrintStream out;

    public EmailChannel(PrintStream out) {
        this.out = out;
    }

    @Override
    public String name() {
        return "email";
    }

    @Override
    public void send(Member to, String subject, String body) {
        out.println("  [email to " + to.email() + "] " + subject + ": " + body);
    }
}
