package edu.campus.library.domain.room;

import edu.campus.library.domain.Identifiable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable room reservation. Built only through {@link Builder} (L9),
 * because it has many fields and several cross-field rules
 * (opening hours, maximum length, end after start).
 */
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

    private RoomReservation(Builder b) {
        this.id = b.id;
        this.roomId = b.roomId;
        this.memberId = b.memberId;
        this.date = b.date;
        this.startHour = b.startHour;
        this.endHour = b.endHour;
        this.purpose = b.purpose;
    }

    public static Builder builder() {
        return new Builder();
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

    /** Builder pattern: step-by-step construction with validation in build(). */
    public static final class Builder {
        private String id;
        private String roomId;
        private String memberId;
        private LocalDate date;
        private int startHour = -1;
        private int endHour = -1;
        private String purpose = "study";

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder room(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public Builder bookedBy(String memberId) {
            this.memberId = memberId;
            return this;
        }

        public Builder on(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder from(int hour) {
            this.startHour = hour;
            return this;
        }

        public Builder to(int hour) {
            this.endHour = hour;
            return this;
        }

        public Builder purpose(String purpose) {
            this.purpose = purpose;
            return this;
        }

        public RoomReservation build() {
            Objects.requireNonNull(id, "id is required");
            Objects.requireNonNull(roomId, "room is required");
            Objects.requireNonNull(memberId, "member is required");
            Objects.requireNonNull(date, "date is required");
            if (startHour < OPENS_AT || endHour > CLOSES_AT) {
                throw new IllegalArgumentException(
                        "Rooms are open " + OPENS_AT + ":00-" + CLOSES_AT + ":00");
            }
            if (endHour <= startHour) {
                throw new IllegalArgumentException("End must be after start");
            }
            if (endHour - startHour > MAX_HOURS) {
                throw new IllegalArgumentException("Maximum booking is " + MAX_HOURS + " hours");
            }
            return new RoomReservation(this);
        }
    }
}
