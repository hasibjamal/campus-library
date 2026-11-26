package edu.campus.library.service.command;

import edu.campus.library.domain.room.RoomReservation;
import edu.campus.library.service.RoomBooking;

public class ReserveRoomCommand implements Command {
    private final RoomBooking booking;
    private final RoomReservation reservation;

    public ReserveRoomCommand(RoomBooking booking, RoomReservation reservation) {
        this.booking = booking;
        this.reservation = reservation;
    }

    @Override
    public void execute() {
        booking.reserve(reservation);
    }

    @Override
    public void undo() {
        booking.cancel(reservation.id());
    }

    @Override
    public String description() {
        return "reserve " + reservation;
    }
}
