package edu.campus.library.domain.room;

import edu.campus.library.domain.Identifiable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/** Immutable room reservation; all rules are checked in the constructor. */
public final class RoomReservation implements Identifiable, Serializable {
    private static final long serialVersionUID = 1L;

    public static final int OPENS_AT = 8;
    public static final int CLOSES_AT = 22;
    public static final int MAX_HOURS = 3;

    private final String id;
    private final String roomId;
    private final String memberId;
    private final LocalDate date;
    private final int startHour;
    private final int endHour;
    private final String purpose;

    public RoomReservation(String id, String roomId, String memberId, LocalDate date,
                           int startHour, int endHour, String purpose) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.roomId = Objects.requireNonNull(roomId, "room is required");
        this.memberId = Objects.requireNonNull(memberId, "member is required");
        this.date = Objects.requireNonNull(date, "date is required");
        if (startHour < OPENS_AT || endHour > CLOSES_AT) {
            throw new IllegalArgumentException("Rooms are open " + OPENS_AT + ":00-" + CLOSES_AT + ":00");
        }
        if (endHour <= startHour) {
            throw new IllegalArgumentException("End must be after start");
        }
        if (endHour - startHour > MAX_HOURS) {
            throw new IllegalArgumentException("Maximum booking is " + MAX_HOURS + " hours");
        }
        this.startHour = startHour;
        this.endHour = endHour;
        this.purpose = purpose == null ? "study" : purpose;
    }

    public boolean overlaps(RoomReservation other) {
        return roomId.equals(other.roomId)
                && date.equals(other.date)
                && startHour < other.endHour
                && other.startHour < endHour;
    }

    @Override
    public String id() {
        return id;
    }

    public String roomId() {
        return roomId;
    }

    public String memberId() {
        return memberId;
    }

    public LocalDate date() {
        return date;
    }

    public int startHour() {
        return startHour;
    }

    public int endHour() {
        return endHour;
    }

    public String purpose() {
        return purpose;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RoomReservation r && r.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s room=%s member=%s %s %02d:00-%02d:00 (%s)",
                id, roomId, memberId, date, startHour, endHour, purpose);
    }
}
