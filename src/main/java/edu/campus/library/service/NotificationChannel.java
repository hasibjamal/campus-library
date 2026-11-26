package edu.campus.library.service;

import edu.campus.library.domain.member.Member;

/**
 * Extension point (Open/Closed): a new channel (SMS, Telegram, push...) is a
 * new class implementing this interface, registered in App. No existing class changes.
 */
public interface NotificationChannel {
    String name();

    void send(Member to, String subject, String body);
}
