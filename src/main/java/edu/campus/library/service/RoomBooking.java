package edu.campus.library.service;

import edu.campus.library.domain.room.Room;
import edu.campus.library.domain.room.RoomReservation;

import java.time.LocalDate;
import java.util.List;

/** Room booking operations. Implemented by the real service and by its protection proxy. */
public interface RoomBooking {
    List<Room> rooms();

    RoomReservation reserve(RoomReservation reservation);

    RoomReservation cancel(String reservationId);

    List<RoomReservation> reservationsOn(LocalDate date);
}
