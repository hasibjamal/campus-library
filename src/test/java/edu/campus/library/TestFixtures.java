package edu.campus.library;

import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.member.MemberType;

import java.time.LocalDate;

public final class TestFixtures {
    public static final LocalDate DAY = LocalDate.of(2026, 9, 1);

    private TestFixtures() {
    }

    public static Member student(String id) {
        return new Member(id, "Student " + id, id + "@campus.edu", MemberType.STUDENT);
    }

    public static Member staff(String id) {
        return new Member(id, "Staff " + id, id + "@campus.edu", MemberType.STAFF);
    }
}
