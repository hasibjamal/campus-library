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

/** Protection Proxy in front of the real room booking service. */
class RoomBookingProxyTest {
    private InMemoryRepository<Member> members;
    private InMemoryRepository<RoomReservation> reservations;
    private RoomBooking proxy;

    @BeforeEach
    void setUp() {
        members = new InMemoryRepository<>();
        members.save(student("S-1"));
        members.save(staff("T-1"));
        InMemoryRepository<Room> rooms = new InMemoryRepository<>();
        rooms.save(new Room("RM-1", "Study", 4, false));
        rooms.save(new Room("RM-3", "Faculty", 20, true));
        reservations = new InMemoryRepository<>();
        proxy = new AccessControlledRoomBooking(new RoomBookingService(rooms, reservations), rooms, members);
    }

    private RoomReservation booking(String id, String room, String member, int from, int to) {
        return RoomReservation.builder().id(id).room(room).bookedBy(member).on(DAY).from(from).to(to).build();
    }

    @Test
    void studentCannotBookStaffOnlyRoom() {
        assertThrows(SecurityException.class, () -> proxy.reserve(booking("R-1", "RM-3", "S-1", 9, 10)));
        assertTrue(reservations.findAll().isEmpty(), "real service never reached");
    }

    @Test
    void staffCanBookStaffOnlyRoom() {
        proxy.reserve(booking("R-1", "RM-3", "T-1", 9, 10));
        assertEquals(1, proxy.reservationsOn(DAY).size());
    }

    @Test
    void inactiveMemberIsBlocked() {
        members.findById("S-1").orElseThrow().deactivate();
        assertThrows(SecurityException.class, () -> proxy.reserve(booking("R-1", "RM-1", "S-1", 9, 10)));
    }

    @Test
    void unknownMemberOrRoomIsReported() {
        assertThrows(NoSuchElementException.class, () -> proxy.reserve(booking("R-1", "RM-1", "X", 9, 10)));
        assertThrows(NoSuchElementException.class, () -> proxy.reserve(booking("R-1", "RM-9", "S-1", 9, 10)));
    }

    @Test
    void realServicePreventsDoubleBooking() {
        proxy.reserve(booking("R-1", "RM-1", "S-1", 9, 11));
        assertThrows(IllegalStateException.class, () -> proxy.reserve(booking("R-2", "RM-1", "T-1", 10, 12)));
        proxy.reserve(booking("R-3", "RM-1", "T-1", 11, 12));
        assertEquals(2, proxy.reservationsOn(DAY).size());
        assertEquals(9, proxy.reservationsOn(DAY).get(0).startHour(), "sorted by time");
    }

    @Test
    void cancelDelegatesAndUnknownIdFails() {
        proxy.reserve(booking("R-1", "RM-1", "S-1", 9, 11));
        assertEquals("R-1", proxy.cancel("R-1").id());
        assertTrue(proxy.reservationsOn(DAY).isEmpty());
        assertThrows(NoSuchElementException.class, () -> proxy.cancel("R-1"));
        assertEquals(2, proxy.rooms().size());
    }
}
