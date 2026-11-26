package edu.campus.library.service;

import edu.campus.library.domain.room.Room;
import edu.campus.library.domain.room.RoomReservation;
import edu.campus.library.infra.persistence.InMemoryRepository;
import edu.campus.library.service.command.CancelRoomCommand;
import edu.campus.library.service.command.CommandHistory;
import edu.campus.library.service.command.ReserveRoomCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static edu.campus.library.TestFixtures.DAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Command + undo: the state after undo is exactly the state before the command. */
class CommandUndoTest {
    private RoomBooking booking;
    private CommandHistory history;

    @BeforeEach
    void setUp() {
        InMemoryRepository<Room> rooms = new InMemoryRepository<>();
        rooms.save(new Room("RM-1", "Study", 4, false));
        booking = new RoomBookingService(rooms, new InMemoryRepository<>());
        history = new CommandHistory();
    }

    private RoomReservation r(String id, int from) {
        return RoomReservation.builder().id(id).room("RM-1").bookedBy("M-1").on(DAY)
                .from(from).to(from + 1).purpose("p" + id).build();
    }

    /** Full snapshot of every field, so "exactly the same" is really checked. */
    private List<String> snapshot() {
        return booking.reservationsOn(DAY).stream().map(RoomReservation::toString).toList();
    }

    @Test
    void undoReserveRestoresPreviousState() {
        history.run(new ReserveRoomCommand(booking, r("R-1", 9)));
        List<String> before = snapshot();
        history.run(new ReserveRoomCommand(booking, r("R-2", 12)));
        assertEquals(2, snapshot().size());
        history.undo();
        assertEquals(before, snapshot());
    }

    @Test
    void undoCancelBringsBackTheSameReservation() {
        history.run(new ReserveRoomCommand(booking, r("R-1", 9)));
        List<String> before = snapshot();
        history.run(new CancelRoomCommand(booking, "R-1"));
        assertTrue(snapshot().isEmpty());
        history.undo();
        assertEquals(before, snapshot());
    }

    @Test
    void undoWalksBackInReverseOrderUntilEmpty() {
        history.run(new ReserveRoomCommand(booking, r("R-1", 9)));
        history.run(new ReserveRoomCommand(booking, r("R-2", 12)));
        history.run(new CancelRoomCommand(booking, "R-1"));
        assertEquals(3, history.descriptions().size());
        assertTrue(history.descriptions().get(0).startsWith("cancel"));
        history.undo();
        history.undo();
        history.undo();
        assertTrue(snapshot().isEmpty());
        assertEquals(Optional.empty(), history.undo());
    }

    @Test
    void failedCommandIsNotRecorded() {
        history.run(new ReserveRoomCommand(booking, r("R-1", 9)));
        assertThrows(IllegalStateException.class,
                () -> history.run(new ReserveRoomCommand(booking, r("R-2", 9))));
        assertEquals(1, history.descriptions().size());
    }

    @Test
    void cancelCommandCannotBeUndoneBeforeExecution() {
        assertThrows(IllegalStateException.class, () -> new CancelRoomCommand(booking, "R-1").undo());
    }
}
