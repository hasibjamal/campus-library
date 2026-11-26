package edu.campus.library.domain;

import edu.campus.library.domain.room.RoomReservation;
import org.junit.jupiter.api.Test;

import static edu.campus.library.TestFixtures.DAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Builder pattern: valid construction and every validation rule. */
class RoomReservationBuilderTest {

    private RoomReservation.Builder valid() {
        return RoomReservation.builder().id("R-1").room("RM-1").bookedBy("M-1").on(DAY).from(10).to(12);
    }

    @Test
    void buildsWithDefaults() {
        RoomReservation r = valid().build();
        assertEquals("RM-1", r.roomId());
        assertEquals("M-1", r.memberId());
        assertEquals(DAY, r.date());
        assertEquals(10, r.startHour());
        assertEquals(12, r.endHour());
        assertEquals("study", r.purpose());
        assertTrue(r.toString().contains("10:00-12:00"));
    }

    @Test
    void rejectsOutsideOpeningHours() {
        assertThrows(IllegalArgumentException.class, () -> valid().from(7).to(9).build());
        assertThrows(IllegalArgumentException.class, () -> valid().from(21).to(23).build());
    }

    @Test
    void rejectsEndBeforeStartAndTooLong() {
        assertThrows(IllegalArgumentException.class, () -> valid().from(12).to(12).build());
        assertThrows(IllegalArgumentException.class, () -> valid().from(9).to(13).build());
    }

    @Test
    void requiresMandatoryFields() {
        assertThrows(NullPointerException.class,
                () -> RoomReservation.builder().room("RM-1").bookedBy("M-1").on(DAY).from(9).to(10).build());
        assertThrows(NullPointerException.class,
                () -> RoomReservation.builder().id("R").bookedBy("M-1").on(DAY).from(9).to(10).build());
        assertThrows(NullPointerException.class,
                () -> RoomReservation.builder().id("R").room("RM-1").on(DAY).from(9).to(10).build());
        assertThrows(NullPointerException.class,
                () -> RoomReservation.builder().id("R").room("RM-1").bookedBy("M").from(9).to(10).build());
    }

    @Test
    void overlapRequiresSameRoomSameDayAndIntersectingHours() {
        RoomReservation a = valid().build();
        assertTrue(a.overlaps(valid().id("R-2").from(11).to(13).build()));
        assertFalse(a.overlaps(valid().id("R-2").from(12).to(14).build()), "touching is fine");
        assertFalse(a.overlaps(valid().id("R-2").room("RM-2").build()));
        assertFalse(a.overlaps(valid().id("R-2").on(DAY.plusDays(1)).build()));
    }

    @Test
    void equalityIsById() {
        assertEquals(valid().build(), valid().purpose("other").build());
        assertNotEquals(valid().build(), valid().id("R-2").build());
        assertEquals(valid().build().hashCode(), valid().build().hashCode());
    }
}
