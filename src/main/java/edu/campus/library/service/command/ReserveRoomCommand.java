package edu.campus.library.service.command;

import edu.campus.library.domain.room.RoomReservation;
import edu.campus.library.service.RoomBookingService;

public class ReserveRoomCommand implements Command {
    private final RoomBookingService booking;
    private final RoomReservation reservation;

    public ReserveRoomCommand(RoomBookingService booking, RoomReservation reservation) {
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
