package edu.campus.library.service;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.room.Room;
import edu.campus.library.domain.room.RoomReservation;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/** Real subject: stores reservations and prevents double booking. */
public class RoomBookingService implements RoomBooking {
    private final Repository<Room> rooms;
    private final Repository<RoomReservation> reservations;

    public RoomBookingService(Repository<Room> rooms, Repository<RoomReservation> reservations) {
        this.rooms = rooms;
        this.reservations = reservations;
    }

    @Override
    public List<Room> rooms() {
        return rooms.findAll();
    }

    @Override
    public RoomReservation reserve(RoomReservation r) {
        rooms.findById(r.roomId()).orElseThrow(() -> new NoSuchElementException("No room " + r.roomId()));
        reservations.findAll().stream()
                .filter(existing -> !existing.id().equals(r.id()) && existing.overlaps(r))
                .findFirst()
                .ifPresent(clash -> {
                    throw new IllegalStateException("Room is already booked: " + clash);
                });
        return reservations.save(r);
    }

    @Override
    public RoomReservation cancel(String reservationId) {
        RoomReservation r = reservations.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("No reservation " + reservationId));
        reservations.deleteById(reservationId);
        return r;
    }

    @Override
    public List<RoomReservation> reservationsOn(LocalDate date) {
        return reservations.findAll().stream()
                .filter(r -> r.date().equals(date))
                .sorted(Comparator.comparing(RoomReservation::roomId).thenComparing(RoomReservation::startHour))
                .toList();
    }
}
