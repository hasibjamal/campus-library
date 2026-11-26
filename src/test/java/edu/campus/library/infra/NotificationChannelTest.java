package edu.campus.library.infra;

import edu.campus.library.infra.notification.SmsChannel;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static edu.campus.library.TestFixtures.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** SMS channel: the variant added through the notification extension point. */
class NotificationChannelTest {
    private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

    private String printed() {
        return buf.toString(StandardCharsets.UTF_8);
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
