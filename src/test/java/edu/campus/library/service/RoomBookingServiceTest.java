package edu.campus.library.service;

import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.room.Room;
import edu.campus.library.domain.room.RoomReservation;
import edu.campus.library.infra.persistence.InMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static edu.campus.library.TestFixtures.DAY;
import static edu.campus.library.TestFixtures.staff;
import static edu.campus.library.TestFixtures.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomBookingServiceTest {
    private InMemoryRepository<Member> members;
    private RoomBookingService service;

    @BeforeEach
    void setUp() {
        members = new InMemoryRepository<>();
        members.save(student("S-1"));
        members.save(staff("T-1"));
        InMemoryRepository<Room> rooms = new InMemoryRepository<>();
        rooms.save(new Room("RM-1", "Study", 4, false));
        rooms.save(new Room("RM-3", "Faculty", 20, true));
        service = new RoomBookingService(rooms, new InMemoryRepository<>(), members);
    }

    private RoomReservation booking(String id, String room, String member, int from, int to) {
        return new RoomReservation(id, room, member, DAY, from, to, "x");
    }

    @Test
    void accessRules() {
        assertThrows(SecurityException.class, () -> service.reserve(booking("R-1", "RM-3", "S-1", 9, 10)));
        service.reserve(booking("R-1", "RM-3", "T-1", 9, 10));
        members.findById("S-1").orElseThrow().deactivate();
        assertThrows(SecurityException.class, () -> service.reserve(booking("R-2", "RM-1", "S-1", 9, 10)));
        assertThrows(NoSuchElementException.class, () -> service.reserve(booking("R-3", "RM-1", "X", 9, 10)));
        assertThrows(NoSuchElementException.class, () -> service.reserve(booking("R-3", "RM-9", "T-1", 9, 10)));
    }

    @Test
    void noDoubleBookingAndSortedSchedule() {
        service.reserve(booking("R-1", "RM-1", "T-1", 11, 12));
        assertThrows(IllegalStateException.class, () -> service.reserve(booking("R-2", "RM-1", "T-1", 11, 13)));
        service.reserve(booking("R-3", "RM-1", "T-1", 9, 11));
        assertEquals(9, service.reservationsOn(DAY).get(0).startHour());
        assertEquals("R-1", service.cancel("R-1").id());
        assertThrows(NoSuchElementException.class, () -> service.cancel("R-1"));
        assertEquals(1, service.reservationsOn(DAY).size());
        assertEquals(2, service.rooms().size());
        assertTrue(service.reservationsOn(DAY.plusDays(1)).isEmpty());
    }
}
