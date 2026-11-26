package edu.campus.library.infra.persistence;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.catalog.CatalogRepository;
import edu.campus.library.domain.loan.Loan;
import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.room.Room;
import edu.campus.library.domain.room.RoomReservation;

import java.nio.file.Path;

/** Creates file-backed repositories, one data file per aggregate, inside one folder. */
public class FileRepositoryFactory implements RepositoryFactory {
    private final Path dir;

    public FileRepositoryFactory(Path dir) {
        this.dir = dir;
    }

    @Override
    public Repository<Member> members() {
        return new FileRepository<>(dir.resolve("members.dat"));
    }

    @Override
    public Repository<Loan> loans() {
        return new FileRepository<>(dir.resolve("loans.dat"));
    }

    @Override
    public Repository<Room> rooms() {
        return new FileRepository<>(dir.resolve("rooms.dat"));
    }

    @Override
    public Repository<RoomReservation> roomReservations() {
        return new FileRepository<>(dir.resolve("room-reservations.dat"));
    }

    @Override
    public CatalogRepository catalog() {
        return new FileCatalogRepository(dir.resolve("catalogue.dat"));
    }
}
