package edu.campus.library.service;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.member.MemberType;
import edu.campus.library.domain.room.Room;
import edu.campus.library.domain.room.RoomReservation;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/** Stores room reservations, checks access rules and prevents double booking. */
public class RoomBookingService {
    private final Repository<Room> rooms;
    private final Repository<RoomReservation> reservations;
    private final Repository<Member> members;

    public RoomBookingService(Repository<Room> rooms, Repository<RoomReservation> reservations,
                              Repository<Member> members) {
        this.rooms = rooms;
        this.reservations = reservations;
        this.members = members;
    }

    public List<Room> rooms() {
        return rooms.findAll();
    }

    public RoomReservation reserve(RoomReservation r) {
        Room room = rooms.findById(r.roomId()).orElseThrow(() -> new NoSuchElementException("No room " + r.roomId()));
        Member member = members.findById(r.memberId())
                .orElseThrow(() -> new NoSuchElementException("No member " + r.memberId()));
        if (!member.isActive()) {
            throw new SecurityException("Inactive members cannot book rooms");
        }
        if (room.staffOnly() && member.type() != MemberType.STAFF) {
            throw new SecurityException("Room " + room.name() + " is for staff only");
        }
        reservations.findAll().stream()
                .filter(existing -> !existing.id().equals(r.id()) && existing.overlaps(r))
                .findFirst()
                .ifPresent(clash -> {
                    throw new IllegalStateException("Room is already booked: " + clash);
                });
        return reservations.save(r);
    }

    public RoomReservation cancel(String reservationId) {
        RoomReservation r = reservations.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("No reservation " + reservationId));
        reservations.deleteById(reservationId);
        return r;
    }

    public List<RoomReservation> reservationsOn(LocalDate date) {
        return reservations.findAll().stream()
                .filter(r -> r.date().equals(date))
                .sorted(Comparator.comparing(RoomReservation::roomId).thenComparing(RoomReservation::startHour))
                .toList();
    }
}
