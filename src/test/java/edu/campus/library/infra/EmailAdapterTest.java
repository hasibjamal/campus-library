package edu.campus.library.infra;

import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.member.MemberType;
import edu.campus.library.infra.notification.EmailChannel;
import edu.campus.library.infra.notification.vendor.QuickMailClient;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static edu.campus.library.TestFixtures.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Adapter: our send(member, subject, body) is translated to the vendor's post(address, html, headers). */
class EmailAdapterTest {

    /** Records what the adapter hands to the vendor SDK. */
    static class RecordingClient extends QuickMailClient {
        String address;
        String html;
        Map<String, String> headers = new HashMap<>();
        int answer = 202;

        RecordingClient() {
            super(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        }

        @Override
        public int post(String toAddress, String htmlBody, Map<String, String> h) {
            address = toAddress;
            html = htmlBody;
            headers = h;
            return answer;
        }
    }

    @Test
    void translatesCallToVendorApi() {
        RecordingClient client = new RecordingClient();
        EmailChannel channel = new EmailChannel(client);
        channel.send(student("M-1"), "Overdue notice", "Return <B-1> & pay");
        assertEquals("email", channel.name());
        assertEquals("M-1@campus.edu", client.address);
        assertEquals("<p>Return &lt;B-1&gt; &amp; pay</p>", client.html);
        assertEquals("Overdue notice", client.headers.get("Subject"));
    }

    @Test
    void vendorRefusalBecomesException() {
        RecordingClient client = new RecordingClient();
        client.answer = 400;
        assertThrows(IllegalStateException.class,
                () -> new EmailChannel(client).send(student("M-1"), "s", "b"));
    }

    @Test
    void realVendorClientPrintsAndRejectsBadAddress() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        QuickMailClient real = new QuickMailClient(new PrintStream(buf, true, StandardCharsets.UTF_8));
        new EmailChannel(real).send(student("M-1"), "Fine charged", "1.00");
        assertTrue(buf.toString(StandardCharsets.UTF_8).contains("[email to M-1@campus.edu] Fine charged: 1.00"));
        Member noMail = new Member("M-9", "No Mail", "none", MemberType.STAFF);
        assertThrows(IllegalStateException.class, () -> new EmailChannel(real).send(noMail, "s", "b"));
    }
}
