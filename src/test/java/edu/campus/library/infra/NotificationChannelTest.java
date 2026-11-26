package edu.campus.library.infra;

import edu.campus.library.infra.notification.EmailChannel;
import edu.campus.library.infra.notification.SmsChannel;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static edu.campus.library.TestFixtures.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The two channels behind the notification extension point. */
class NotificationChannelTest {
    private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

    private String printed() {
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void emailUsesAddressAndSubject() {
        EmailChannel c = new EmailChannel(out);
        c.send(student("M-1"), "Overdue notice", "Return it");
        assertEquals("email", c.name());
        assertTrue(printed().contains("[email to M-1@campus.edu] Overdue notice: Return it"));
    }

    @Test
    void smsIsShortenedTo160Characters() {
        SmsChannel c = new SmsChannel(out);
        c.send(student("M-1"), "S", "x".repeat(300));
        assertEquals("sms", c.name());
        String text = printed().substring(printed().indexOf("] ") + 2).trim();
        assertEquals(160, text.length());
        assertTrue(text.endsWith("..."));
    }
}
