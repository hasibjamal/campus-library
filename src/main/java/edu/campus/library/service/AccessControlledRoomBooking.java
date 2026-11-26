package edu.campus.library.service;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.member.MemberType;
import edu.campus.library.domain.room.Room;
import edu.campus.library.domain.room.RoomReservation;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Protection Proxy (L11). Same interface as {@link RoomBookingService}, but
 * refuses staff-only rooms to students and inactive members before
 * delegating. The real service stays free of access rules.
 */
public class AccessControlledRoomBooking implements RoomBooking {
    private final RoomBooking target;
    private final Repository<Room> rooms;
    private final Repository<Member> members;

    public AccessControlledRoomBooking(RoomBooking target, Repository<Room> rooms, Repository<Member> members) {
        this.target = target;
        this.rooms = rooms;
        this.members = members;
    }

    @Override
    public List<Room> rooms() {
        return target.rooms();
    }

    @Override
    public RoomReservation reserve(RoomReservation r) {
        Member member = members.findById(r.memberId())
                .orElseThrow(() -> new NoSuchElementException("No member " + r.memberId()));
        Room room = rooms.findById(r.roomId())
                .orElseThrow(() -> new NoSuchElementException("No room " + r.roomId()));
        if (!member.isActive()) {
            throw new SecurityException("Inactive members cannot book rooms");
        }
        if (room.staffOnly() && member.type() != MemberType.STAFF) {
            throw new SecurityException("Room " + room.name() + " is for staff only");
        }
        return target.reserve(r);
    }

    @Override
    public RoomReservation cancel(String reservationId) {
        return target.cancel(reservationId);
    }

    @Override
    public List<RoomReservation> reservationsOn(LocalDate date) {
        return target.reservationsOn(date);
    }
}
