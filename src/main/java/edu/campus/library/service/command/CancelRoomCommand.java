package edu.campus.library.service.command;

import edu.campus.library.domain.room.RoomReservation;
import edu.campus.library.service.RoomBookingService;

public class CancelRoomCommand implements Command {
    private final RoomBookingService booking;
    private final String reservationId;
    private RoomReservation cancelled;

    public CancelRoomCommand(RoomBookingService booking, String reservationId) {
        this.booking = booking;
        this.reservationId = reservationId;
    }

    @Override
    public void execute() {
        cancelled = booking.cancel(reservationId);
    }

    @Override
    public void undo() {
        if (cancelled == null) {
            throw new IllegalStateException("Nothing to undo");
        }
        booking.reserve(cancelled);
    }

    @Override
    public String description() {
        return "cancel reservation " + reservationId;
    }
}
