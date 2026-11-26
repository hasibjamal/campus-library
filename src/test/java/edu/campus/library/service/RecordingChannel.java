package edu.campus.library.service;

import edu.campus.library.domain.member.Member;

import java.util.ArrayList;
import java.util.List;

/** Test double for the notification extension point. */
class RecordingChannel implements NotificationChannel {
    final List<String> sent = new ArrayList<>();

    @Override
    public String name() {
        return "recording";
    }

    @Override
    public void send(Member to, String subject, String body) {
        sent.add(to.id() + "|" + subject + "|" + body);
    }
}
