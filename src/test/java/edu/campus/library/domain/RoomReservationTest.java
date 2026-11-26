package edu.campus.library.domain;

import edu.campus.library.domain.room.RoomReservation;
import org.junit.jupiter.api.Test;

import static edu.campus.library.TestFixtures.DAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomReservationTest {

    private RoomReservation r(String id, String room, int from, int to) {
        return new RoomReservation(id, room, "M-1", DAY, from, to, null);
    }

    @Test
    void validReservation() {
        RoomReservation a = r("R-1", "RM-1", 10, 12);
        assertEquals("study", a.purpose());
        assertEquals("M-1", a.memberId());
        assertEquals(DAY, a.date());
        assertEquals(10, a.startHour());
        assertEquals(12, a.endHour());
        assertTrue(a.toString().contains("10:00-12:00"));
    }

    @Test
    void rulesAreChecked() {
        assertThrows(IllegalArgumentException.class, () -> r("R", "RM-1", 7, 9));
        assertThrows(IllegalArgumentException.class, () -> r("R", "RM-1", 21, 23));
        assertThrows(IllegalArgumentException.class, () -> r("R", "RM-1", 12, 12));
        assertThrows(IllegalArgumentException.class, () -> r("R", "RM-1", 9, 13));
        assertThrows(NullPointerException.class, () -> r(null, "RM-1", 9, 10));
    }

    @Test
    void overlapAndEquality() {
        RoomReservation a = r("R-1", "RM-1", 10, 12);
        assertTrue(a.overlaps(r("R-2", "RM-1", 11, 13)));
        assertFalse(a.overlaps(r("R-2", "RM-1", 12, 14)));
        assertFalse(a.overlaps(r("R-2", "RM-2", 10, 12)));
        assertEquals(a, r("R-1", "RM-2", 9, 10));
        assertNotEquals(a, r("R-2", "RM-1", 10, 12));
        assertEquals(a.hashCode(), r("R-1", "RM-1", 10, 12).hashCode());
    }
}
